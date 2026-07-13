package com.ebchat.ui.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")

    // Main Flow
    object Main : Screen("main")
    object Home : Screen("home")
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object GroupChat : Screen("group_chat/{groupId}") {
        fun createRoute(groupId: String) = "group_chat/$groupId"
    }
    object Groups : Screen("groups")
    object CreateGroup : Screen("create_group")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object Stories : Screen("stories")
    object StoryViewer : Screen("story_viewer/{userId}") {
        fun createRoute(userId: String) = "story_viewer/$userId"
    }
    object CreateStory : Screen("create_story")
    object PostFeed : Screen("post_feed")
    object CreatePost : Screen("create_post")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object Search : Screen("search")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object Settings : Screen("settings")
    object Themes : Screen("themes")
    object Notifications : Screen("notifications")
    object BlockedUsers : Screen("blocked_users")
    object MutedUsers : Screen("muted_users")
    object Privacy : Screen("privacy")
    object About : Screen("about")
    object Help : Screen("help")
}

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN = "main"
    const val HOME = "home"
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail"
    const val GROUP_CHAT = "group_chat"
    const val GROUPS = "groups"
    const val CREATE_GROUP = "create_group"
    const val GROUP_DETAIL = "group_detail"
    const val STORIES = "stories"
    const val STORY_VIEWER = "story_viewer"
    const val CREATE_STORY = "create_story"
    const val POST_FEED = "post_feed"
    const val CREATE_POST = "create_post"
    const val POST_DETAIL = "post_detail"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val USER_PROFILE = "user_profile"
    const val SETTINGS = "settings"
    const val THEMES = "themes"
    const val NOTIFICATIONS = "notifications"
    const val BLOCKED_USERS = "blocked_users"
    const val MUTED_USERS = "muted_users"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"
    const val HELP = "help"
}
