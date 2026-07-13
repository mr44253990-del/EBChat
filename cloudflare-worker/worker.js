/**
 * EBChat Cloudflare Worker - Push Notification Service
 * 
 * This Worker handles push notifications via Firebase Cloud Messaging (FCM)
 * It receives notification requests and forwards them to FCM for delivery
 * 
 * Deploy: wrangler deploy
 */

// Firebase Server Key (from service account)
const FIREBASE_PROJECT_ID = "chat-4e1d0";
const FIREBASE_CLIENT_EMAIL = "firebase-adminsdk-fbsvc@chat-4e1d0.iam.gserviceaccount.com";

// CORS headers
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Max-Age": "86400",
};

export default {
  async fetch(request, env, ctx) {
    // Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    const url = new URL(request.url);
    const path = url.pathname;

    try {
      // Health check
      if (path === "/" || path === "/health") {
        return jsonResponse({ status: "ok", service: "EBChat Notification Worker", timestamp: new Date().toISOString() });
      }

      // Send notification to a single device
      if (path === "/send" && request.method === "POST") {
        return await sendNotification(request, env);
      }

      // Send notification to multiple devices
      if (path === "/send-multicast" && request.method === "POST") {
        return await sendMulticast(request, env);
      }

      // Send to topic
      if (path === "/send-topic" && request.method === "POST") {
        return await sendToTopic(request, env);
      }

      // Register device token
      if (path === "/register-token" && request.method === "POST") {
        return await registerToken(request, env);
      }

      // Unregister device token
      if (path === "/unregister-token" && request.method === "POST") {
        return await unregisterToken(request, env);
      }

      return jsonResponse({ error: "Not found" }, 404);
    } catch (error) {
      console.error("Worker error:", error);
      return jsonResponse({ error: error.message }, 500);
    }
  }
};

/**
 * Send notification to a single device
 */
async function sendNotification(request, env) {
  const body = await request.json();
  const { token, title, body: messageBody, data = {}, sound = "default" } = body;

  if (!token || !title || !messageBody) {
    return jsonResponse({ error: "Missing required fields: token, title, body" }, 400);
  }

  const accessToken = await getAccessToken(env);

  const fcmPayload = {
    message: {
      token: token,
      notification: {
        title: title,
        body: messageBody,
      },
      data: Object.fromEntries(
        Object.entries(data).map(([k, v]) => [k, String(v)])
      ),
      android: {
        priority: "high",
        notification: {
          sound: sound,
          channelId: "ebchat_messages_channel",
          priority: "high",
          defaultSound: true,
          defaultVibrateTimings: true,
          defaultLightSettings: true,
          notificationCount: 1,
        }
      },
      apns: {
        payload: {
          aps: {
            sound: sound,
            badge: 1,
          }
        }
      },
      webpush: {
        headers: {
          Urgency: "high"
        },
        notification: {
          icon: "https://ebchat.app/icon.png",
          badge: "https://ebchat.app/badge.png",
          tag: data.chatId || "default",
          requireInteraction: true,
          actions: [
            { action: "reply", title: "Reply" },
            { action: "mark_read", title: "Mark Read" }
          ]
        }
      }
    }
  };

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(fcmPayload),
    }
  );

  const result = await response.json();

  if (!response.ok) {
    console.error("FCM send failed:", result);
    return jsonResponse({ error: "Failed to send notification", details: result }, 500);
  }

  return jsonResponse({ success: true, messageId: result.name });
}

/**
 * Send notification to multiple devices
 */
async function sendMulticast(request, env) {
  const body = await request.json();
  const { tokens, title, body: messageBody, data = {} } = body;

  if (!tokens || !Array.isArray(tokens) || tokens.length === 0) {
    return jsonResponse({ error: "Missing or invalid tokens array" }, 400);
  }

  // FCM v1 doesn't have multicast, so we send individually but batch them
  const results = await Promise.allSettled(
    tokens.map(token =>
      sendSingleNotification(env, token, title, messageBody, data)
    )
  );

  const successCount = results.filter(r => r.status === "fulfilled").length;
  const failureCount = results.filter(r => r.status === "rejected").length;

  return jsonResponse({
    success: true,
    successCount,
    failureCount,
    total: tokens.length
  });
}

/**
 * Send notification to a topic
 */
async function sendToTopic(request, env) {
  const body = await request.json();
  const { topic, title, body: messageBody, data = {} } = body;

  if (!topic || !title || !messageBody) {
    return jsonResponse({ error: "Missing required fields: topic, title, body" }, 400);
  }

  const accessToken = await getAccessToken(env);

  const fcmPayload = {
    message: {
      topic: topic,
      notification: {
        title: title,
        body: messageBody,
      },
      data: Object.fromEntries(
        Object.entries(data).map(([k, v]) => [k, String(v)])
      ),
      android: {
        priority: "high",
        notification: {
          channelId: "ebchat_default_channel",
          priority: "high",
        }
      }
    }
  };

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(fcmPayload),
    }
  );

  const result = await response.json();

  if (!response.ok) {
    return jsonResponse({ error: "Failed to send topic notification", details: result }, 500);
  }

  return jsonResponse({ success: true, messageId: result.name });
}

/**
 * Register a device token to a user in KV storage
 */
async function registerToken(request, env) {
  const body = await request.json();
  const { userId, token } = body;

  if (!userId || !token) {
    return jsonResponse({ error: "Missing userId or token" }, 400);
  }

  // Store in KV
  await env.EBCHAT_TOKENS.put(`user:${userId}`, token);
  await env.EBCHAT_TOKENS.put(`token:${token}`, userId);

  return jsonResponse({ success: true, message: "Token registered" });
}

/**
 * Unregister a device token
 */
async function unregisterToken(request, env) {
  const body = await request.json();
  const { userId, token } = body;

  if (userId) {
    const storedToken = await env.EBCHAT_TOKENS.get(`user:${userId}`);
    if (storedToken) {
      await env.EBCHAT_TOKENS.delete(`token:${storedToken}`);
    }
    await env.EBCHAT_TOKENS.delete(`user:${userId}`);
  }

  if (token) {
    const storedUser = await env.EBCHAT_TOKENS.get(`token:${token}`);
    if (storedUser) {
      await env.EBCHAT_TOKENS.delete(`user:${storedUser}`);
    }
    await env.EBCHAT_TOKENS.delete(`token:${token}`);
  }

  return jsonResponse({ success: true, message: "Token unregistered" });
}

/**
 * Helper: Send a single notification
 */
async function sendSingleNotification(env, token, title, body, data) {
  const accessToken = await getAccessToken(env);

  const fcmPayload = {
    message: {
      token: token,
      notification: { title, body },
      data: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, String(v)])),
      android: {
        priority: "high",
        notification: {
          channelId: "ebchat_messages_channel",
          priority: "high",
        }
      }
    }
  };

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(fcmPayload),
    }
  );

  if (!response.ok) {
    throw new Error(`FCM error: ${response.status}`);
  }

  return response.json();
}

/**
 * Get OAuth2 access token for FCM
 */
async function getAccessToken(env) {
  const cacheKey = "fcm_access_token";
  
  // Check cache
  const cached = await env.EBCHAT_TOKENS.get(cacheKey);
  if (cached) {
    try {
      const parsed = JSON.parse(cached);
      if (parsed.expiresAt > Date.now()) {
        return parsed.token;
      }
    } catch (e) {
      // Invalid cache, continue
    }
  }

  // Get private key from env
  const privateKey = env.FIREBASE_PRIVATE_KEY;
  if (!privateKey) {
    throw new Error("FIREBASE_PRIVATE_KEY not configured");
  }

  // Create JWT
  const now = Math.floor(Date.now() / 1000);
  const jwtHeader = btoa(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const jwtClaim = btoa(JSON.stringify({
    iss: FIREBASE_CLIENT_EMAIL,
    sub: FIREBASE_CLIENT_EMAIL,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    iat: now,
    exp: now + 3600,
  }));

  const signingInput = `${jwtHeader}.${jwtClaim}`;
  
  // Sign with Web Crypto API
  const keyData = privateKey
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\\n/g, "")
    .trim();
  
  const binaryKey = Uint8Array.from(atob(keyData), c => c.charCodeAt(0));
  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryKey.buffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
  
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    new TextEncoder().encode(signingInput)
  );
  
  const jwtSignature = btoa(String.fromCharCode(...new Uint8Array(signature)));
  const jwt = `${signingInput}.${jwtSignature}`;

  // Exchange JWT for access token
  const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });

  const tokenData = await tokenResponse.json();
  
  if (!tokenResponse.ok) {
    throw new Error(`Token exchange failed: ${JSON.stringify(tokenData)}`);
  }

  // Cache token
  await env.EBCHAT_TOKENS.put(cacheKey, JSON.stringify({
    token: tokenData.access_token,
    expiresAt: Date.now() + (tokenData.expires_in * 1000) - 60000, // 1 min buffer
  }));

  return tokenData.access_token;
}

/**
 * Helper: JSON response
 */
function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json",
      ...corsHeaders,
    },
  });
}
