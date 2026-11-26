package com.example.data

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    UZ("uz", "Oʻzbek", "Uzbek"),
    EN("en", "English", "English"),
    RU("ru", "Русский", "Russian"),
}

interface AppStrings {
    val appName: String
    val connecting: String
    val updating: String
    val waitingForNetwork: String
    // Bottom nav
    val tabChats: String
    val tabContacts: String
    val tabSettings: String
    val tabProfile: String
    // Auth
    val welcomeTitle: String
    val yourPhoneNumber: String
    val phoneSubtitle: String
    val countryLabel: String
    val chooseCountry: String
    val searchCountry: String
    val phoneNumberLabel: String
    val signInQr: String
    val signInPhone: String
    val enterCode: String
    val codeSentToTelegram: String
    val codeSentSms: String
    val codeSentCall: String
    val codeSentGeneric: String
    val resendCode: String
    val wrongNumber: String
    val twoStepVerification: String
    val cloudPasswordHint: String
    val passwordHintPrefix: String
    val password: String
    val registerTitle: String
    val registerSubtitle: String
    val firstName: String
    val lastName: String
    val qrScanInstruction: String
    val copyLoginLink: String
    val linkCopied: String
    val continueAction: String
    // Chat list
    val searchChatsHint: String
    val allChats: String
    val archivedChats: String
    val savedMessages: String
    val noChatsYet: String
    val noChatsSubtitle: String
    val startMessaging: String
    val localSearchHeader: String
    val globalSearchHeader: String
    val noSearchResults: String
    val draftPrefix: String
    val youPrefix: String
    val pinChat: String
    val unpinChat: String
    val mute: String
    val unmute: String
    val archive: String
    val unarchive: String
    val markUnread: String
    val markRead: String
    val deleteChat: String
    val deleteChatConfirm: String
    val delete: String
    val cancel: String
    // Chat screen
    val messageHint: String
    val botStartButton: String
    val botMenuButtonDefault: String
    val botCommandsTitle: String
    val pinnedMessage: String
    val noMessagesYet: String
    val typing: String
    val recordingVoice: String
    val sendingMedia: String
    val online: String
    val lastSeenRecently: String
    val lastSeenWithinWeek: String
    val lastSeenWithinMonth: String
    val lastSeenJustNow: String
    val lastSeenAt: String
    val offline: String
    val subscribersCount: String
    val membersCount: String
    val cloudStorage: String
    val joinChannel: String
    val leaveChannel: String
    val leaveGroup: String
    val reply: String
    val forward: String
    val copy: String
    val edit: String
    val pin: String
    val unpin: String
    val editedLabel: String
    val forwardedFrom: String
    val editMessageTitle: String
    val replyToPrefix: String
    val gallery: String
    val fileLabel: String
    val forwardTo: String
    val deleteMessageTitle: String
    val deleteMessageConfirm: String
    val deleteForEveryone: String
    val clearHistory: String
    val infoAndMedia: String
    val micPermissionRequired: String
    val today: String
    val yesterday: String
    val openingWebApp: String
    val webAppClose: String
    val openInBrowser: String
    val reload: String
    // Contacts
    val searchContactsHint: String
    val sortByLastSeen: String
    val sortByName: String
    val newGroup: String
    val newChannel: String
    val newSecretChat: String
    val groupNameHint: String
    val channelNameHint: String
    val channelDescHint: String
    val channelCreateSubtitle: String
    val createAction: String
    val noContactsFound: String
    // Profile
    val setPhoto: String
    val editInfo: String
    val infoSection: String
    val mobileLabel: String
    val usernameLabel: String
    val birthdateLabel: String
    val bioLabel: String
    val personalChannel: String
    val myQrCode: String
    val postsTab: String
    val savedTab: String
    val giftsTab: String
    // Settings Hub & Subscreens
    val accountsSection: String
    val addAccount: String
    val accountTitle: String
    val accountSubtitle: String
    val chatSettingsTitle: String
    val chatSettingsSubtitle: String
    val privacyTitle: String
    val privacySubtitle: String
    val notificationsTitle: String
    val notificationsSubtitle: String
    val dataStorageTitle: String
    val dataStorageSubtitle: String
    val chatFoldersTitle: String
    val chatFoldersSubtitle: String
    val devicesTitle: String
    val devicesSubtitle: String
    val powerSavingTitle: String
    val powerSavingSubtitle: String
    val languageTitle: String
    val logOut: String
    val logOutConfirm: String
    // Account edit
    val yourNameSection: String
    val bioHint: String
    val bioHelper: String
    val yourInfoSection: String
    val tapToChangeUsername: String
    val tapToSetBirthdate: String
    val saveAction: String
    val savedToast: String
    // Chat settings
    val messageTextSize: String
    val changeChatWallpaper: String
    val changeNameColor: String
    val colorThemeSection: String
    val switchToDayMode: String
    val switchToNightMode: String
    val messageCorners: String
    val chatListView: String
    val twoLineView: String
    val threeLineView: String
    val swipeGestureTitle: String
    val swipeDelete: String
    val swipeArchive: String
    val swipePin: String
    val swipeFolder: String
    val autoNightMode: String
    val inAppBrowser: String
    val inAppBrowserDesc: String
    val animationsLabel: String
    val sendWithEnter: String
    // Privacy & Security
    val securitySection: String
    val passcodeLock: String
    val passcodeEnabled: String
    val passcodeDisabled: String
    val enterPasscodeTitle: String
    val setPasscodeHint: String
    val blockedUsers: String
    val privacySection: String
    val phonePrivacy: String
    val lastSeenPrivacy: String
    val photoPrivacy: String
    val forwardPrivacy: String
    val callsPrivacy: String
    val bioPrivacy: String
    val everybody: String
    val myContacts: String
    val nobody: String
    val deleteAccountIfAway: String
    val monthsFormat: String
    // Notifications
    val privateChats: String
    val groupsLabel: String
    val channelsLabel: String
    val messagePreview: String
    // Data & Storage
    val clearCache: String
    val cacheCleared: String
    val totalCacheUsed: String
    // Chat Folders
    val foldersHeroDesc: String
    val recommendedFolders: String
    val addFolderAction: String
    val createNewFolder: String
    val folderNameHint: String
    val folderDisplayStyle: String
    val showIconStyle: String
    val showNameStyle: String
    val unreadFolderName: String
    val unreadFolderDesc: String
    val personalFolderName: String
    val personalFolderDesc: String
    // Devices
    val devicesHeroDesc: String
    val thisDeviceSection: String
    val terminateAllOtherSessions: String
    val activeSessionsSection: String
    val sessionTerminated: String
    val allOtherSessionsTerminated: String
    // Power saving
    val powerSavingHero: String
    val batteryThreshold: String
    val smoothAnimations: String
    // Auth & Multi-account
    val syncContacts: String
    val addNewAccount: String
    // Voice / Video Note / Attach / Poll
    val slideToCancel: String
    val holdForVideoTapForVoice: String
    val holdForVoiceTapForVideo: String
    val cameraPermissionRequired: String
    val musicLabel: String
    val pollLabel: String
    val locationLabel: String
    val contactLabel: String
    val createPollTitle: String
    val pollQuestionHint: String
    val pollOptionHint: String
    val anonymousPoll: String
    val multipleAnswers: String
    val tapToDownload: String
    // Channel / Group Profile & Edit
    val publicChannel: String
    val privateChannel: String
    val publicGroup: String
    val privateGroup: String
    val liveStream: String
    val videoChat: String
    val discussion: String
    val descriptionLabel: String
    val inviteLinkLabel: String
    val subscribersLabel: String
    val adminsLabel: String
    val bannedLabel: String
    val membersLabel: String
    val channelSettings: String
    val groupSettings: String
    val addMembers: String
    val ownerBadge: String
    val adminBadge: String
    val setNewPhoto: String
    val channelTypeLabel: String
    val groupTypeLabel: String
    val linkedChannelLabel: String
    val reactionsLabel: String
    val permissionsLabel: String
    val deleteChannel: String
    val deleteAndLeaveGroup: String
    val publicLabel: String
    val privateLabel: String
    // Data, Storage & Network Donut
    val storageAndNetworkSection: String
    val storageUsageTitle: String
    val networkUsageTitle: String
    val saveLocationTitle: String
    val internalStorage: String
    val autoMediaDownloadSection: String
    val whenUsingMobile: String
    val whenUsingWifi: String
    val whenUsingRoaming: String
    val notEnabled: String
    val enabledLabel: String
    val resetAutoDownload: String
    val saveToGallerySection: String
    val offLabel: String
    val streamingSection: String
    val streamVideoAudio: String
    val streamVideoAudioDesc: String
    val callsSection: String
    val useLessData: String
    val onlyWhileRoaming: String
    val proxySection: String
    val proxySettings: String
    val clearDrafts: String
    val clearCacheBtn: String
    val storageCloudNote: String
    val allTab: String
    val mobileTab: String
    val wifiTab: String
    val roamingTab: String
    val chatsTab: String
    val mediaTab: String
    val filesTab: String
    val musicTab: String
    val totalNetworkUsage: String
    val sentData: String
    val receivedData: String
    val resetStatistics: String
    val stickersAndEmojis: String
    val videosLabel: String
    val photosLabel: String
    val documentsLabel: String
    val voiceMessagesLabel: String
    val profilePhotosLabel: String
    val otherLabel: String
    val messagesLabel: String
    val previewPhoto: String
    val previewVideo: String
    val previewVoice: String
    val previewSticker: String
    val previewVideoNote: String
    val previewLocation: String
    val previewContact: String
    val previewVoiceCall: String
    val previewVideoCall: String
    val previewMessage: String
    // Stories, Gifts, Premium, Stars, Help, Custom Emoji
    val myStory: String
    val storiesLabel: String
    val addPost: String
    val archivedPostsTab: String
    val archivedStoriesHint: String
    val postToProfile: String
    val archiveStory: String
    val deleteStory: String
    val noStoriesYet: String
    val allGifts: String
    val addCollection: String
    val giftsTapHint: String
    val sendGiftsToFriends: String
    val noGiftsYet: String
    val convertToStars: String
    val hideFromProfile: String
    val showOnProfile: String
    val senderAnonymous: String
    val sendGiftTitle: String
    val telegramPremiumTitle: String
    val telegramStarsTitle: String
    val helpSection: String
    val askQuestion: String
    val telegramFaq: String
    val telegramFeatures: String
    val privacyPolicy: String
    val askVolunteerTitle: String
    val askVolunteerBody: String
    val askVolunteerBtn: String
    val supportCenterTitle: String
    val supportRoleSubtitle: String
    val supportPhoneDisplay: String
    val supportBioDisplay: String
    val supportWelcomePill: String
    val addToContacts: String
    val supportAdminPanel: String
    val yourBalance: String
    val earnStars: String
    val earnStarsDesc: String
    val allTransactionsTab: String
    val incomingTab: String
    val outgoingTab: String
    val giftConverted: String
    val starReaction: String
    val topUpBalance: String
    val appFooterVersion: String
    val customEmojiPackTitle: String
    val addEmojiPack: String
    val emojiPackAdded: String
    val callLabel: String
    val ageYearsFormat: String
}

private object UzbekStrings : AppStrings {
    override val appName = "Ghostly Messenger"
    override val connecting = "Ulanmoqda…"
    override val updating = "Yangilanmoqda…"
    override val waitingForNetwork = "Tarmoq kutilmoqda…"
    override val tabChats = "Chatlar"
    override val tabContacts = "Kontaktlar"
    override val tabSettings = "Sozlamalar"
    override val tabProfile = "Profil"
    override val welcomeTitle = "Telegramga xush kelibsiz"
    override val yourPhoneNumber = "Telefon raqamingiz"
    override val phoneSubtitle = "Mamlakat kodini tasdiqlang va telefon raqamingizni kiriting."
    override val countryLabel = "Mamlakat"
    override val chooseCountry = "Mamlakatni tanlang"
    override val searchCountry = "Mamlakat yoki kodni qidiring"
    override val phoneNumberLabel = "Telefon raqami"
    override val signInQr = "QR kod orqali kirish"
    override val signInPhone = "Telefon raqami orqali kirish"
    override val enterCode = "Tasdiqlash kodi"
    override val codeSentToTelegram = "Kod boshqa qurilmadagi Telegram ilovangizga yuborildi."
    override val codeSentSms = "%s raqamiga SMS kod yuborildi."
    override val codeSentCall = "%s raqamiga qoʻngʻiroq qilinmoqda."
    override val codeSentGeneric = "%s raqamiga yuborilgan kodni kiriting."
    override val resendCode = "Kodni qayta yuborish"
    override val wrongNumber = "Raqam notoʻgʻrimi?"
    override val twoStepVerification = "Ikki bosqichli tekshiruv"
    override val cloudPasswordHint = "Hisobingiz qoʻshimcha bulutli parol bilan himoyalangan."
    override val passwordHintPrefix = "Maslahat: "
    override val password = "Parol"
    override val registerTitle = "Maʼlumotlaringiz"
    override val registerSubtitle = "Ismingizni kiriting va profilingizni yarating."
    override val firstName = "Ismingiz (majburiy)"
    override val lastName = "Familiya (ixtiyoriy)"
    override val qrScanInstruction = "Boshqa qurilmada Telegram -> Sozlamalar -> Qurilmalar -> Qurilma ulashni oching yoki havoladan foydalaning."
    override val copyLoginLink = "tg://login havolasini nusxalash"
    override val linkCopied = "Havola nusxalandi"
    override val continueAction = "Davom etish"
    override val searchChatsHint = "Chatlarni qidirish"
    override val allChats = "Barcha chatlar"
    override val archivedChats = "Arxivlangan chatlar"
    override val savedMessages = "Saqlangan xabarlar"
    override val noChatsYet = "Hozircha chatlar yoʻq"
    override val noChatsSubtitle = "Yangi suhbat boshlash yoki bot va kanallarni qidirish uchun pastdagi tugmani bosing."
    override val startMessaging = "Suhbatni boshlash"
    override val localSearchHeader = "Chatlar va kontaktlar"
    override val globalSearchHeader = "Global qidiruv (Botlar, Kanallar)"
    override val noSearchResults = "Mos chat, bot yoki kanal topilmadi"
    override val draftPrefix = "Qoralama: "
    override val youPrefix = "Siz: "
    override val pinChat = "Yuqoriga qadash"
    override val unpinChat = "Qadashdan yechish"
    override val mute = "Ovozini oʻchirish"
    override val unmute = "Ovozini yoqish"
    override val archive = "Arxivlash"
    override val unarchive = "Arxivdan chiqarish"
    override val markUnread = "Oʻqilmagan deb belgilash"
    override val markRead = "Oʻqilgan deb belgilash"
    override val deleteChat = "Chatni oʻchirish"
    override val deleteChatConfirm = "Ushbu chatni oʻchirishni xohlaysizmi?"
    override val delete = "Oʻchirish"
    override val cancel = "Bekor qilish"
    override val messageHint = "Xabar"
    override val botStartButton = "BOSHLASH (/start)"
    override val botMenuButtonDefault = "Menyu"
    override val botCommandsTitle = "Bot buyruqlari"
    override val pinnedMessage = "Qadalgan xabar"
    override val noMessagesYet = "Bu yerda hali xabarlar yoʻq…"
    override val typing = "yozmoqda…"
    override val recordingVoice = "ovozli xabar yozmoqda…"
    override val sendingMedia = "media yubormoqda…"
    override val online = "onlayn"
    override val lastSeenRecently = "yaqinda onlayn edi"
    override val lastSeenWithinWeek = "shu haftada onlayn edi"
    override val lastSeenWithinMonth = "shu oyda onlayn edi"
    override val lastSeenJustNow = "hozirgina onlayn edi"
    override val lastSeenAt = "oxirgi marta: %s da"
    override val offline = "oflayn"
    override val subscribersCount = "%d ta obunachi"
    override val membersCount = "%d ta aʼzo"
    override val cloudStorage = "bulutli xotira"
    override val joinChannel = "QOʻSHILISH"
    override val leaveChannel = "Kanaldan chiqish"
    override val leaveGroup = "Guruhdan chiqish"
    override val reply = "Javob berish"
    override val forward = "Uzatish"
    override val copy = "Nusxa olish"
    override val edit = "Tahrirlash"
    override val pin = "Qadash"
    override val unpin = "Qadashdan olish"
    override val editedLabel = "tahrirlangan"
    override val forwardedFrom = "Uzatilgan: "
    override val editMessageTitle = "Xabarni tahrirlash"
    override val replyToPrefix = "Javob: "
    override val gallery = "Galereya"
    override val fileLabel = "Fayl"
    override val forwardTo = "Qayerga uzatish…"
    override val deleteMessageTitle = "Xabarni oʻchirish"
    override val deleteMessageConfirm = "Haqiqatan ham ushbu xabarni oʻchirmoqchimisiz?"
    override val deleteForEveryone = "Barcha uchun oʻchirish"
    override val clearHistory = "Tarixni tozalash"
    override val infoAndMedia = "Maʼlumot va media"
    override val micPermissionRequired = "Ovozli xabar yozish uchun mikrofon ruxsati kerak."
    override val today = "Bugun"
    override val yesterday = "Kecha"
    override val openingWebApp = "Mini ilova ochilmoqda…"
    override val webAppClose = "Yopish"
    override val openInBrowser = "Brauzerda ochish"
    override val reload = "Qayta yuklash"
    override val searchContactsHint = "Kontaktlarni qidirish"
    override val sortByLastSeen = "Oxirgi faollik boʻyicha tartiblash"
    override val sortByName = "Ism boʻyicha tartiblash"
    override val newGroup = "Yangi guruh"
    override val newChannel = "Yangi kanal"
    override val newSecretChat = "Yangi maxfiy chat"
    override val groupNameHint = "Guruh nomi"
    override val channelNameHint = "Kanal nomi"
    override val channelDescHint = "Tavsif (ixtiyoriy)"
    override val channelCreateSubtitle = "Cheksiz obunachilarga xabar ulashish uchun ommaviy yoki shaxsiy kanal yarating."
    override val createAction = "Yaratish"
    override val noContactsFound = "Kontaktlar topilmadi"
    override val setPhoto = "Rasm belgilash"
    override val editInfo = "Tahrirlash"
    override val infoSection = "Axborot"
    override val mobileLabel = "Mobil raqam"
    override val usernameLabel = "Foydalanuvchi nomi"
    override val birthdateLabel = "Tugʻilgan kun"
    override val bioLabel = "Tarjimayi hol"
    override val personalChannel = "Shaxsiy kanal"
    override val myQrCode = "Mening QR kodim"
    override val postsTab = "Postlar"
    override val savedTab = "Saqlangan"
    override val giftsTab = "Hadyalar"
    override val accountsSection = "Hisoblar"
    override val addAccount = "Hisob maʼlumotlari"
    override val accountTitle = "Hisob"
    override val accountSubtitle = "Raqam, foydalanuvchi nomi, tarjimayi hol"
    override val chatSettingsTitle = "Chat sozlamalari"
    override val chatSettingsSubtitle = "Fon rasmi, tungi rejim, animatsiyalar"
    override val privacyTitle = "Maxfiylik va xavfsizlik"
    override val privacySubtitle = "Oxirgi faollik, qurilmalar, kirish kalitlari"
    override val notificationsTitle = "Bildirishnomalar"
    override val notificationsSubtitle = "Tovushlar, chaqiruvlar, nishonlar"
    override val dataStorageTitle = "Maʼlumotlar va xotira"
    override val dataStorageSubtitle = "Media yuklab olish va kesh"
    override val chatFoldersTitle = "Chat jildlari"
    override val chatFoldersSubtitle = "Chatlarni jildlarga saralash"
    override val devicesTitle = "Qurilmalar"
    override val devicesSubtitle = "Ulangan qurilmalarni boshqarish"
    override val powerSavingTitle = "Quvvat tejash"
    override val powerSavingSubtitle = "Animatsiya va energiya nazorati"
    override val languageTitle = "Til"
    override val logOut = "Chiqish"
    override val logOutConfirm = "Ushbu Telegram seansidan chiqishni xohlaysizmi?"
    override val yourNameSection = "Ismingiz"
    override val bioHint = "Tarjimayi hol"
    override val bioHelper = "Oʻzingiz haqingizda bir nechta soʻz."
    override val yourInfoSection = "Axborotlaringiz"
    override val tapToChangeUsername = "Foydalanuvchi nomini tahrirlash"
    override val tapToSetBirthdate = "Tugʻilgan kunni belgilash"
    override val saveAction = "Saqlash"
    override val savedToast = "Saqlandi"
    override val messageTextSize = "Xabarlar matni oʻlchami"
    override val changeChatWallpaper = "Chat fon rasmlarini oʻzgartirish"
    override val changeNameColor = "Ism va urgʻu rangini oʻzgartirish"
    override val colorThemeSection = "Rangli mavzu"
    override val switchToDayMode = "Kunduzgi rejimga almashtirish"
    override val switchToNightMode = "Tungi rejimga almashtirish"
    override val messageCorners = "Xabarlar burchaklari"
    override val chatListView = "Chat roʻyxati koʻrinishi"
    override val twoLineView = "Ikki qatorli"
    override val threeLineView = "Uch qatorli"
    override val swipeGestureTitle = "Chatlar roʻyxatini surish ishorasi"
    override val swipeDelete = "Oʻchirish"
    override val swipeArchive = "Arxivlash"
    override val swipePin = "Qadash"
    override val swipeFolder = "Oʻqilmagan"
    override val autoNightMode = "Avtomatik tun rejimi"
    override val inAppBrowser = "Ilova ichidagi brauzer"
    override val inAppBrowserDesc = "Tashqi havola va Mini ilovalarni ilova ichida ochish"
    override val animationsLabel = "Animatsiyalar"
    override val sendWithEnter = "Enter tugmasi bilan yuborish"
    override val securitySection = "Xavfsizlik"
    override val passcodeLock = "Kirish kodi bilan qulflash"
    override val passcodeEnabled = "Yoqilgan"
    override val passcodeDisabled = "Oʻchiq"
    override val enterPasscodeTitle = "Kirish kodini kiriting"
    override val setPasscodeHint = "4 xonali PIN kod kiriting (oʻchirish uchun boʻsh qoldiring)"
    override val blockedUsers = "Bloklangan foydalanuvchilar"
    override val privacySection = "Maxfiylik"
    override val phonePrivacy = "Telefon raqami"
    override val lastSeenPrivacy = "Oxirgi faollik va onlayn"
    override val photoPrivacy = "Profil rasmlari"
    override val forwardPrivacy = "Uzatilgan xabarlar"
    override val callsPrivacy = "Chaqiruvlar"
    override val bioPrivacy = "Tarjimayi hol"
    override val everybody = "Hamma"
    override val myContacts = "Kontaktlarim"
    override val nobody = "Hech kim"
    override val deleteAccountIfAway = "Hisobni avtomatik oʻchirish"
    override val monthsFormat = "%d oy"
    override val privateChats = "Shaxsiy chatlar"
    override val groupsLabel = "Guruhlar"
    override val channelsLabel = "Kanallar"
    override val messagePreview = "Xabar matnini koʻrsatish"
    override val clearCache = "Mahalliy keshni tozalash"
    override val cacheCleared = "Kesh tozalandi"
    override val totalCacheUsed = "Band qilingan xotira: %s"
    override val foldersHeroDesc = "Turli chatlar guruhi uchun jildlar yarating va ular orasida tezkor harakatlaning."
    override val recommendedFolders = "Tavsiya etilgan jildlar"
    override val addFolderAction = "Qoʻshish"
    override val createNewFolder = "Yangi jild yaratish"
    override val folderNameHint = "Jild nomi"
    override val folderDisplayStyle = "Jildlar koʻrinishi"
    override val showIconStyle = "Koʻrsatiladigan belgi"
    override val showNameStyle = "Koʻrsatiladigan nom"
    override val unreadFolderName = "Oʻqilmagan"
    override val unreadFolderDesc = "Barcha chatlardan yangi xabarlar."
    override val personalFolderName = "Shaxsiy"
    override val personalFolderDesc = "Faqat shaxsiy chatlardan xabarlar."
    override val devicesHeroDesc = "Ushbu hisobga ulangan faol Telegram qurilmalari va seanslari."
    override val thisDeviceSection = "Bu qurilma"
    override val terminateAllOtherSessions = "Boshqa barcha seanslarni tugatish"
    override val activeSessionsSection = "Faol seanslar"
    override val sessionTerminated = "Seans tugatildi"
    override val allOtherSessionsTerminated = "Boshqa barcha seanslar tugatildi"
    override val powerSavingHero = "Quvvat kamayganda interfeys animatsiyalari va effektlarni kamaytirish."
    override val batteryThreshold = "Quvvat chegarasi"
    override val smoothAnimations = "Silliq animatsiyalar"
    override val syncContacts = "Kontaktlarni sinxronlash"
    override val addNewAccount = "Hisob qoʻshish"
    override val slideToCancel = "Bekor qilish uchun suring"
    override val holdForVideoTapForVoice = "Video yozish uchun tugmani bosib turing.\nOvozli xabar uchun bir marta bosing."
    override val holdForVoiceTapForVideo = "Ovozli xabar uchun bosib turing.\nVideo xabar uchun bir marta bosing."
    override val cameraPermissionRequired = "Video xabar yozish uchun kamera va mikrofon ruxsati kerak."
    override val musicLabel = "Musiqa"
    override val pollLabel = "Soʻrovnoma"
    override val locationLabel = "Joylashuv"
    override val contactLabel = "Kontakt"
    override val createPollTitle = "Yangi soʻrovnoma"
    override val pollQuestionHint = "Savol"
    override val pollOptionHint = "Variant %d"
    override val anonymousPoll = "Anonim ovoz berish"
    override val multipleAnswers = "Bir nechta javob"
    override val tapToDownload = "Yuklab olish uchun bosing"
    override val publicChannel = "ommaviy kanal"
    override val privateChannel = "shaxsiy kanal"
    override val publicGroup = "ommaviy guruh"
    override val privateGroup = "shaxsiy guruh"
    override val liveStream = "Jonli efir"
    override val videoChat = "Videochat"
    override val discussion = "Muhokama"
    override val descriptionLabel = "Tavsif"
    override val inviteLinkLabel = "Taklif havolasi"
    override val subscribersLabel = "Obunachilar"
    override val adminsLabel = "Adminlar"
    override val bannedLabel = "Bloklanganlar"
    override val membersLabel = "Aʼzolar"
    override val channelSettings = "Kanal sozlamalari"
    override val groupSettings = "Guruh sozlamalari"
    override val addMembers = "Aʼzolar qoʻshish"
    override val ownerBadge = "Ega"
    override val adminBadge = "Admin"
    override val setNewPhoto = "Yangi rasm belgilash"
    override val channelTypeLabel = "Kanal turi"
    override val groupTypeLabel = "Guruh turi"
    override val linkedChannelLabel = "Biriktirilgan kanal"
    override val reactionsLabel = "Reaksiyalar"
    override val permissionsLabel = "Ruxsatlar"
    override val deleteChannel = "Kanalni oʻchirish"
    override val deleteAndLeaveGroup = "Guruhni oʻchirish va tark etish"
    override val publicLabel = "Ommaviy"
    override val privateLabel = "Shaxsiy"
    override val storageAndNetworkSection = "Xotira va tarmoqdan foydalanish"
    override val storageUsageTitle = "Xotiradan foydalanish"
    override val networkUsageTitle = "Maʼlumotlardan foydalanish"
    override val saveLocationTitle = "Saqlash joyi"
    override val internalStorage = "Ichki xotira"
    override val autoMediaDownloadSection = "Media fayllarni avtomatik yuklab olish"
    override val whenUsingMobile = "Mobil internet orqali ulanganda"
    override val whenUsingWifi = "Wi-Fi tarmoqqa ulanganda"
    override val whenUsingRoaming = "Rouming orqali ulanganda"
    override val notEnabled = "Yoqilmagan"
    override val enabledLabel = "Yoqilgan"
    override val resetAutoDownload = "Avtomatik yuklab olish sozlamalarini asliga qaytarish"
    override val saveToGallerySection = "Galereyaga saqlash"
    override val offLabel = "Oʻchiq"
    override val streamingSection = "Oqimli uzatish"
    override val streamVideoAudio = "Video va audio fayllarni oqimli uzatish"
    override val streamVideoAudioDesc = "Telegram imkon boʻlganida video va musiqalar toʻliq yuklab olinishini kutmasdan ijro qilishni boshlaydi."
    override val callsSection = "Chaqiruvlar"
    override val useLessData = "Chaqiruvda trafik tejash"
    override val onlyWhileRoaming = "Faqat roumingda"
    override val proxySection = "Proksi"
    override val proxySettings = "Proksi sozlamalari"
    override val clearDrafts = "Qoralamalarni oʻchirish"
    override val clearCacheBtn = "Keshni tozalash %s"
    override val storageCloudNote = "Barcha media fayllar Telegram bulutida qoladi va ularni kerak boʻlganda qayta yuklab olishingiz mumkin."
    override val allTab = "Hammasi"
    override val mobileTab = "Mobil"
    override val wifiTab = "Wi-Fi"
    override val roamingTab = "Rouming"
    override val chatsTab = "Chatlar"
    override val mediaTab = "Media"
    override val filesTab = "Fayllar"
    override val musicTab = "Musiqa"
    override val totalNetworkUsage = "Jami tarmoqdan foydalanish"
    override val sentData = "Yuborilgan maʼlumotlar"
    override val receivedData = "Qabul qilingan maʼlumotlar"
    override val resetStatistics = "Statistikani asliga qaytarish"
    override val stickersAndEmojis = "Stikerlar va emojilar"
    override val videosLabel = "Videolar"
    override val photosLabel = "Rasmlar"
    override val documentsLabel = "Hujjatlar"
    override val voiceMessagesLabel = "Ovozli xabarlar"
    override val profilePhotosLabel = "Profil rasmlari"
    override val otherLabel = "Boshqa"
    override val messagesLabel = "Xabarlar"
    override val previewPhoto = "Rasm"
    override val previewVideo = "Video"
    override val previewVoice = "Ovozli xabar"
    override val previewSticker = "Stiker"
    override val previewVideoNote = "Videoxabar"
    override val previewLocation = "Joylashuv"
    override val previewContact = "Kontakt"
    override val previewVoiceCall = "Ovozli qoʻngʻiroq"
    override val previewVideoCall = "Videoqoʻngʻiroq"
    override val previewMessage = "Xabar"
    override val myStory = "Mening hikoyam"
    override val storiesLabel = "Hikoyalar"
    override val addPost = "Post qoʻshish"
    override val archivedPostsTab = "Arxivlangan postlar"
    override val archivedStoriesHint = "Agar arxivlangan hikoyalarni profilga joylamasangiz, ularni faqat siz koʻra olasiz."
    override val postToProfile = "Profilga joylash"
    override val archiveStory = "Arxivga olish"
    val deleteStoryLabel = "Hikoyani oʻchirish"
    override val deleteStory = "Hikoyani oʻchirish"
    override val noStoriesYet = "Hozircha postlar yoʻq"
    override val allGifts = "Barcha hadyalar"
    override val addCollection = "+ Kolleksiya qoʻshish"
    override val giftsTapHint = "Hadyani yulduzlarga aylantirish yoki uning maxfiylik sozlamalarini oʻzgartirish uchun ustiga bosing."
    override val sendGiftsToFriends = "Doʻstlarga hadyalar yuborish"
    override val noGiftsYet = "Hozircha hadyalar yoʻq"
    override val convertToStars = "%d ⭐ yulduzga almashtirish"
    override val hideFromProfile = "Profildan yashirish"
    override val showOnProfile = "Profilda koʻrsatish"
    override val senderAnonymous = "Anonim yuboruvchi"
    override val sendGiftTitle = "Hadya yuborish"
    override val telegramPremiumTitle = "Telegram Premium"
    override val telegramStarsTitle = "Telegram Yulduzlar"
    override val helpSection = "Yordam"
    override val askQuestion = "Savol berish"
    override val telegramFaq = "Telegram haqida savollar"
    override val telegramFeatures = "Telegram funksiyalari"
    override val privacyPolicy = "Maxfiylik siyosati"
    override val askVolunteerTitle = "Savol berish"
    override val askVolunteerBody = "Unutmang, Ghostly Messenger qoʻllab-quvvatlash xizmati faqat koʻngillilar tomonidan yuritiladi. Biz imkon qadar tezroq javob berishga harakat qilamiz, lekin bu biroz vaqt olishi mumkin.\n\nTelegram haqida savollar boʻlimiga qarang: unda koʻplab savollarga javoblar va muammolarni bartaraf etish boʻyicha muhim maslahatlar berilgan."
    override val askVolunteerBtn = "Koʻngillidan soʻrash"
    override val supportCenterTitle = "Qoʻllab-quvvatlash Markazi"
    override val supportRoleSubtitle = "yordam"
    override val supportPhoneDisplay = "+47777"
    override val supportBioDisplay = "Ushbu akkaunt Ghostly Messenger rasmiy jamoasi tomonidan yuritiladi"
    override val supportWelcomePill = "Ghostly Messenger haqida\nsavolingiz bormi?"
    override val addToContacts = "Kontaktlarga qoʻshish"
    override val supportAdminPanel = "Admin Murojaatlari"
    override val yourBalance = "balansingiz"
    override val earnStars = "Yulduzlar ishlash"
    override val earnStarsDesc = "Mini ilovalarga havolalarni tarqating va ularning daromadidan yulduzlarda ulush ishlab toping."
    override val allTransactionsTab = "Barcha tranzaksiyalar"
    override val incomingTab = "Kiruvchi"
    override val outgoingTab = "Chiquvchi"
    override val giftConverted = "🧸 Hadya oʻgirildi"
    override val starReaction = "Yulduz reaksiyasi"
    override val topUpBalance = "Hisobni toʻldirish"
    override val appFooterVersion = "Ghostly Messenger for Android v1.0.0\nstore bundled arm64-v8a"
    override val customEmojiPackTitle = "Emoji toʻplami"
    override val addEmojiPack = "Toʻplamni qoʻshish"
    override val emojiPackAdded = "Toʻplam qoʻshildi"
    override val callLabel = "Chaqiruv"
    override val ageYearsFormat = "(%d yosh)"
}

private object EnglishStrings : AppStrings {
    override val appName = "Ghostly Messenger"
    override val connecting = "Connecting…"
    override val updating = "Updating…"
    override val waitingForNetwork = "Waiting for network…"
    override val tabChats = "Chats"
    override val tabContacts = "Contacts"
    override val tabSettings = "Settings"
    override val tabProfile = "Profile"
    override val welcomeTitle = "Welcome to Telegram"
    override val yourPhoneNumber = "Your Phone Number"
    override val phoneSubtitle = "Please confirm your country code and enter your phone number."
    override val countryLabel = "Country"
    override val chooseCountry = "Choose a country"
    override val searchCountry = "Search country or code"
    override val phoneNumberLabel = "Phone number"
    override val signInQr = "Sign in with QR Code"
    override val signInPhone = "Sign in with Phone Number"
    override val enterCode = "Enter Code"
    override val codeSentToTelegram = "We've sent the code to your Telegram app on another device."
    override val codeSentSms = "We've sent an SMS with an activation code to %s."
    override val codeSentCall = "We are calling %s to dictate the code."
    override val codeSentGeneric = "Enter the verification code sent to %s."
    override val resendCode = "Resend Code"
    override val wrongNumber = "Wrong number?"
    override val twoStepVerification = "Two-Step Verification"
    override val cloudPasswordHint = "Your account is protected with an additional cloud password."
    override val passwordHintPrefix = "Hint: "
    override val password = "Password"
    override val registerTitle = "Your Info"
    override val registerSubtitle = "Enter your name and set up your profile."
    override val firstName = "First name (required)"
    override val lastName = "Last name (optional)"
    override val qrScanInstruction = "Open Telegram on another device -> Settings -> Devices -> Link Desktop Device, or copy login link."
    override val copyLoginLink = "Copy tg://login link"
    override val linkCopied = "Link copied"
    override val continueAction = "Continue"
    override val searchChatsHint = "Search chats"
    override val allChats = "All Chats"
    override val archivedChats = "Archived Chats"
    override val savedMessages = "Saved Messages"
    override val noChatsYet = "No chats yet"
    override val noChatsSubtitle = "Tap the button below to message contacts or search bots and channels."
    override val startMessaging = "Start Messaging"
    override val localSearchHeader = "Chats & Contacts"
    override val globalSearchHeader = "Global Search (Bots, Channels)"
    override val noSearchResults = "No matching chats, bots or channels found"
    override val draftPrefix = "Draft: "
    override val youPrefix = "You: "
    override val pinChat = "Pin"
    override val unpinChat = "Unpin"
    override val mute = "Mute"
    override val unmute = "Unmute"
    override val archive = "Archive"
    override val unarchive = "Unarchive"
    override val markUnread = "Mark as Unread"
    override val markRead = "Mark as Read"
    override val deleteChat = "Delete chat"
    override val deleteChatConfirm = "Delete chat with %s?"
    override val delete = "Delete"
    override val cancel = "Cancel"
    override val messageHint = "Message"
    override val botStartButton = "START (/start)"
    override val botMenuButtonDefault = "Menu"
    override val botCommandsTitle = "Bot Commands"
    override val pinnedMessage = "Pinned Message"
    override val noMessagesYet = "No messages here yet…"
    override val typing = "typing…"
    override val recordingVoice = "recording voice…"
    override val sendingMedia = "sending media…"
    override val online = "online"
    override val lastSeenRecently = "last seen recently"
    override val lastSeenWithinWeek = "last seen within a week"
    override val lastSeenWithinMonth = "last seen within a month"
    override val lastSeenJustNow = "last seen just now"
    override val lastSeenAt = "last seen at %s"
    override val offline = "offline"
    override val subscribersCount = "%d subscribers"
    override val membersCount = "%d members"
    override val cloudStorage = "cloud storage"
    override val joinChannel = "JOIN"
    override val leaveChannel = "Leave Channel"
    override val leaveGroup = "Leave Group"
    override val reply = "Reply"
    override val forward = "Forward"
    override val copy = "Copy"
    override val edit = "Edit"
    override val pin = "Pin"
    override val unpin = "Unpin"
    override val editedLabel = "edited"
    override val forwardedFrom = "Forwarded from "
    override val editMessageTitle = "Edit message"
    override val replyToPrefix = "Reply to "
    override val gallery = "Gallery"
    override val fileLabel = "File"
    override val forwardTo = "Forward to…"
    override val deleteMessageTitle = "Delete message"
    override val deleteMessageConfirm = "Are you sure you want to delete this message?"
    override val deleteForEveryone = "Also delete for everyone"
    override val clearHistory = "Clear history"
    override val infoAndMedia = "Info & Media"
    override val micPermissionRequired = "Microphone permission is required to record voice messages."
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val openingWebApp = "Opening Mini App…"
    override val webAppClose = "Close"
    override val openInBrowser = "Open in Browser"
    override val reload = "Reload"
    override val searchContactsHint = "Search contacts"
    override val sortByLastSeen = "Sort by last seen"
    override val sortByName = "Sort by name"
    override val newGroup = "New Group"
    override val newChannel = "New Channel"
    override val newSecretChat = "New Secret Chat"
    override val groupNameHint = "Group name"
    override val channelNameHint = "Channel name"
    override val channelDescHint = "Description (optional)"
    override val channelCreateSubtitle = "Create a public or private channel to broadcast to unlimited subscribers."
    override val createAction = "Create"
    override val noContactsFound = "No contacts found"
    override val setPhoto = "Set Photo"
    override val editInfo = "Edit Info"
    override val infoSection = "Info"
    override val mobileLabel = "Mobile"
    override val usernameLabel = "Username"
    override val birthdateLabel = "Birthdate"
    override val bioLabel = "Bio"
    override val personalChannel = "Personal Channel"
    override val myQrCode = "My QR Code"
    override val postsTab = "Posts"
    override val savedTab = "Saved"
    override val giftsTab = "Gifts"
    override val accountsSection = "Accounts"
    override val addAccount = "Account Details"
    override val accountTitle = "Account"
    override val accountSubtitle = "Phone number, username, bio"
    override val chatSettingsTitle = "Chat Settings"
    override val chatSettingsSubtitle = "Wallpaper, night mode, animations"
    override val privacyTitle = "Privacy and Security"
    override val privacySubtitle = "Last seen, devices, passcode lock"
    override val notificationsTitle = "Notifications"
    override val notificationsSubtitle = "Sounds, calls, badges"
    override val dataStorageTitle = "Data and Storage"
    override val dataStorageSubtitle = "Media downloads and cache"
    override val chatFoldersTitle = "Chat Folders"
    override val chatFoldersSubtitle = "Organize chats into folders"
    override val devicesTitle = "Devices"
    override val devicesSubtitle = "Manage connected sessions"
    override val powerSavingTitle = "Power Saving"
    override val powerSavingSubtitle = "Animations and battery options"
    override val languageTitle = "Language"
    override val logOut = "Log Out"
    override val logOutConfirm = "Are you sure you want to log out of this Telegram session?"
    override val yourNameSection = "Your Name"
    override val bioHint = "Bio"
    override val bioHelper = "Any details such as age, occupation or city."
    override val yourInfoSection = "Your Info"
    override val tapToChangeUsername = "Edit username"
    override val tapToSetBirthdate = "Set birthdate"
    override val saveAction = "Save"
    override val savedToast = "Saved"
    override val messageTextSize = "Message Text Size"
    override val changeChatWallpaper = "Change Chat Wallpaper"
    override val changeNameColor = "Change Accent Color"
    override val colorThemeSection = "Color Theme"
    override val switchToDayMode = "Switch to Day Mode"
    override val switchToNightMode = "Switch to Night Mode"
    override val messageCorners = "Message Corners"
    override val chatListView = "Chat List View"
    override val twoLineView = "Two-line"
    override val threeLineView = "Three-line"
    override val swipeGestureTitle = "Chat List Swipe Gesture"
    override val swipeDelete = "Delete"
    override val swipeArchive = "Archive"
    override val swipePin = "Pin"
    override val swipeFolder = "Unread"
    override val autoNightMode = "Auto-Night Mode"
    override val inAppBrowser = "In-App Browser"
    override val inAppBrowserDesc = "Open external links and Mini Apps inside the app"
    override val animationsLabel = "Animations"
    override val sendWithEnter = "Send by Enter"
    override val securitySection = "Security"
    override val passcodeLock = "Passcode Lock"
    override val passcodeEnabled = "On"
    override val passcodeDisabled = "Off"
    override val enterPasscodeTitle = "Enter Passcode"
    override val setPasscodeHint = "Enter 4-digit PIN (leave empty to disable)"
    override val blockedUsers = "Blocked Users"
    override val privacySection = "Privacy"
    override val phonePrivacy = "Phone Number"
    override val lastSeenPrivacy = "Last Seen & Online"
    override val photoPrivacy = "Profile Photos"
    override val forwardPrivacy = "Forwarded Messages"
    override val callsPrivacy = "Calls"
    override val bioPrivacy = "Bio"
    override val everybody = "Everybody"
    override val myContacts = "My Contacts"
    override val nobody = "Nobody"
    override val deleteAccountIfAway = "Delete my account if away for"
    override val monthsFormat = "%d months"
    override val privateChats = "Private Chats"
    override val groupsLabel = "Groups"
    override val channelsLabel = "Channels"
    override val messagePreview = "Message Preview"
    override val clearCache = "Clear Local Cache"
    override val cacheCleared = "Cache cleared"
    override val totalCacheUsed = "Used storage: %s"
    override val foldersHeroDesc = "Create folders for different groups of chats and quickly switch between them."
    override val recommendedFolders = "Recommended Folders"
    override val addFolderAction = "Add"
    override val createNewFolder = "Create New Folder"
    override val folderNameHint = "Folder name"
    override val folderDisplayStyle = "Folder Tab Style"
    override val showIconStyle = "Show Icons"
    override val showNameStyle = "Show Names"
    override val unreadFolderName = "Unread"
    override val unreadFolderDesc = "New messages from all chats."
    override val personalFolderName = "Personal"
    override val personalFolderDesc = "Messages from personal chats only."
    override val devicesHeroDesc = "Active Telegram devices and sessions logged into your account."
    override val thisDeviceSection = "This Device"
    override val terminateAllOtherSessions = "Terminate All Other Sessions"
    override val activeSessionsSection = "Active Sessions"
    override val sessionTerminated = "Session terminated"
    override val allOtherSessionsTerminated = "All other sessions terminated"
    override val powerSavingHero = "Reduce interface animations and visual effects when saving battery."
    override val batteryThreshold = "Battery threshold"
    override val smoothAnimations = "Smooth animations"
    override val syncContacts = "Sync Contacts"
    override val addNewAccount = "Add Account"
    override val slideToCancel = "Slide to cancel"
    override val holdForVideoTapForVoice = "Hold to record video.\nTap to switch to voice."
    override val holdForVoiceTapForVideo = "Hold to record voice.\nTap to switch to video."
    override val cameraPermissionRequired = "Camera and microphone permissions are required to record video notes."
    override val musicLabel = "Music"
    override val pollLabel = "Poll"
    override val locationLabel = "Location"
    override val contactLabel = "Contact"
    override val createPollTitle = "New Poll"
    override val pollQuestionHint = "Question"
    override val pollOptionHint = "Option %d"
    override val anonymousPoll = "Anonymous Voting"
    override val multipleAnswers = "Multiple Answers"
    override val tapToDownload = "Tap to download"
    override val publicChannel = "public channel"
    override val privateChannel = "private channel"
    override val publicGroup = "public group"
    override val privateGroup = "private group"
    override val liveStream = "Live Stream"
    override val videoChat = "Video Chat"
    override val discussion = "Discussion"
    override val descriptionLabel = "Description"
    override val inviteLinkLabel = "Invite Link"
    override val subscribersLabel = "Subscribers"
    override val adminsLabel = "Administrators"
    override val bannedLabel = "Removed Users"
    override val membersLabel = "Members"
    override val channelSettings = "Channel Settings"
    override val groupSettings = "Group Settings"
    override val addMembers = "Add Members"
    override val ownerBadge = "Owner"
    override val adminBadge = "Admin"
    override val setNewPhoto = "Set New Photo"
    override val channelTypeLabel = "Channel Type"
    override val groupTypeLabel = "Group Type"
    override val linkedChannelLabel = "Linked Channel"
    override val reactionsLabel = "Reactions"
    override val permissionsLabel = "Permissions"
    override val deleteChannel = "Delete Channel"
    override val deleteAndLeaveGroup = "Delete and Leave Group"
    override val publicLabel = "Public"
    override val privateLabel = "Private"
    override val storageAndNetworkSection = "Disk and network usage"
    override val storageUsageTitle = "Storage Usage"
    override val networkUsageTitle = "Data Usage"
    override val saveLocationTitle = "Save Path"
    override val internalStorage = "Internal Storage"
    override val autoMediaDownloadSection = "Automatic media download"
    override val whenUsingMobile = "When using mobile data"
    override val whenUsingWifi = "When connected on Wi-Fi"
    override val whenUsingRoaming = "When roaming"
    override val notEnabled = "Disabled"
    override val enabledLabel = "Enabled"
    override val resetAutoDownload = "Reset Auto-Download Settings"
    override val saveToGallerySection = "Save to Gallery"
    override val offLabel = "Off"
    override val streamingSection = "Streaming"
    override val streamVideoAudio = "Stream Videos and Audio Files"
    override val streamVideoAudioDesc = "Start playing videos and audio immediately when possible."
    override val callsSection = "Calls"
    override val useLessData = "Use Less Data for Calls"
    override val onlyWhileRoaming = "Only while roaming"
    override val proxySection = "Proxy"
    override val proxySettings = "Proxy Settings"
    override val clearDrafts = "Clear All Drafts"
    override val clearCacheBtn = "Clear Cache %s"
    override val storageCloudNote = "All media stays in the Telegram cloud and can be re-downloaded whenever needed."
    override val allTab = "All"
    override val mobileTab = "Mobile"
    override val wifiTab = "Wi-Fi"
    override val roamingTab = "Roaming"
    override val chatsTab = "Chats"
    override val mediaTab = "Media"
    override val filesTab = "Files"
    override val musicTab = "Music"
    override val totalNetworkUsage = "Total Network Usage"
    override val sentData = "Bytes Sent"
    override val receivedData = "Bytes Received"
    override val resetStatistics = "Reset Statistics"
    override val stickersAndEmojis = "Stickers & Emoji"
    override val videosLabel = "Videos"
    override val photosLabel = "Photos"
    override val documentsLabel = "Documents"
    override val voiceMessagesLabel = "Voice Messages"
    override val profilePhotosLabel = "Profile Photos"
    override val otherLabel = "Other"
    override val messagesLabel = "Messages"
    override val previewPhoto = "Photo"
    override val previewVideo = "Video"
    override val previewVoice = "Voice message"
    override val previewSticker = "Sticker"
    override val previewVideoNote = "Video message"
    override val previewLocation = "Location"
    override val previewContact = "Contact"
    override val previewVoiceCall = "Voice call"
    override val previewVideoCall = "Video call"
    override val previewMessage = "Message"
    override val myStory = "My Story"
    override val storiesLabel = "Stories"
    override val addPost = "Add Post"
    override val archivedPostsTab = "Archived Posts"
    override val archivedStoriesHint = "Unless you post archived stories to your profile, only you can see them."
    override val postToProfile = "Post to Profile"
    override val archiveStory = "Archive Story"
    override val deleteStory = "Delete Story"
    override val noStoriesYet = "No posts yet"
    override val allGifts = "All Gifts"
    override val addCollection = "+ Add Collection"
    override val giftsTapHint = "Tap on a gift to convert it to Stars or change its privacy settings."
    override val sendGiftsToFriends = "Send Gifts to Friends"
    override val noGiftsYet = "No gifts yet"
    override val convertToStars = "Convert to %d ⭐"
    override val hideFromProfile = "Hide from Profile"
    override val showOnProfile = "Show on Profile"
    override val senderAnonymous = "Anonymous sender"
    override val sendGiftTitle = "Send a Gift"
    override val telegramPremiumTitle = "Telegram Premium"
    override val telegramStarsTitle = "Telegram Stars"
    override val helpSection = "Help"
    override val askQuestion = "Ask a Question"
    override val telegramFaq = "Telegram FAQ"
    override val telegramFeatures = "Telegram Features"
    override val privacyPolicy = "Privacy Policy"
    override val askVolunteerTitle = "Ask a Question"
    override val askVolunteerBody = "Please note that Ghostly Messenger Support is run by volunteers. We try to reply as quickly as possible, but it may take some time.\n\nPlease check the Telegram FAQ first: it has answers to most questions and troubleshooting tips."
    override val askVolunteerBtn = "Ask a Volunteer"
    override val supportCenterTitle = "Support Center"
    override val supportRoleSubtitle = "support"
    override val supportPhoneDisplay = "+47777"
    override val supportBioDisplay = "This account is operated by the official Ghostly Messenger team"
    override val supportWelcomePill = "Have a question about\nGhostly Messenger?"
    override val addToContacts = "Add to Contacts"
    override val supportAdminPanel = "Admin Support Inbox"
    override val yourBalance = "your balance"
    override val earnStars = "Earn Stars"
    override val earnStarsDesc = "Share links to mini apps and earn a share of their revenue in Stars."
    override val allTransactionsTab = "All Transactions"
    override val incomingTab = "Incoming"
    override val outgoingTab = "Outgoing"
    override val giftConverted = "🧸 Gift converted"
    override val starReaction = "Star reaction"
    override val topUpBalance = "Top up balance"
    override val appFooterVersion = "Ghostly Messenger for Android v1.0.0\nstore bundled arm64-v8a"
    override val customEmojiPackTitle = "Emoji Pack"
    override val addEmojiPack = "Add Emoji Pack"
    override val emojiPackAdded = "Pack added"
    override val callLabel = "Call"
    override val ageYearsFormat = "(%d y.o.)"
}

private object RussianStrings : AppStrings {
    override val appName = "Ghostly Messenger"
    override val connecting = "Соединение…"
    override val updating = "Обновление…"
    override val waitingForNetwork = "Ожидание сети…"
    override val tabChats = "Чаты"
    override val tabContacts = "Контакты"
    override val tabSettings = "Настройки"
    override val tabProfile = "Профиль"
    override val welcomeTitle = "Добро пожаловать в Telegram"
    override val yourPhoneNumber = "Ваш номер телефона"
    override val phoneSubtitle = "Проверьте код страны и введите номер телефона."
    override val countryLabel = "Страна"
    override val chooseCountry = "Выберите страну"
    override val searchCountry = "Поиск страны или кода"
    override val phoneNumberLabel = "Номер телефона"
    override val signInQr = "Войти по QR-коду"
    override val signInPhone = "Войти по номеру телефона"
    override val enterCode = "Введите код"
    override val codeSentToTelegram = "Мы отправили код в приложение Telegram на другом устройстве."
    override val codeSentSms = "Мы отправили SMS с кодом на номер %s."
    override val codeSentCall = "Мы звоним на %s, чтобы продиктовать код."
    override val codeSentGeneric = "Введите код подтверждения, отправленный на %s."
    override val resendCode = "Отправить код повторно"
    override val wrongNumber = "Неверный номер?"
    override val twoStepVerification = "Облачный пароль"
    override val cloudPasswordHint = "Ваш аккаунт защищён дополнительным паролем."
    override val passwordHintPrefix = "Подсказка: "
    override val password = "Пароль"
    override val registerTitle = "Ваши данные"
    override val registerSubtitle = "Введите имя и настройте профиль."
    override val firstName = "Имя (обязательно)"
    override val lastName = "Фамилия (необязательно)"
    override val qrScanInstruction = "Откройте Telegram на другом устройстве -> Настройки -> Устройства -> Подключить устройство."
    override val copyLoginLink = "Скопировать ссылку tg://login"
    override val linkCopied = "Ссылка скопирована"
    override val continueAction = "Продолжить"
    override val searchChatsHint = "Поиск чатов"
    override val allChats = "Все чаты"
    override val archivedChats = "Архив"
    override val savedMessages = "Избранное"
    override val noChatsYet = "Пока нет чатов"
    override val noChatsSubtitle = "Нажмите кнопку ниже, чтобы начать общение или найти ботов и каналы."
    override val startMessaging = "Начать общение"
    override val localSearchHeader = "Чаты и контакты"
    override val globalSearchHeader = "Глобальный поиск (Боты, Каналы)"
    override val noSearchResults = "Подходящих чатов, ботов или каналов не найдено"
    override val draftPrefix = "Черновик: "
    override val youPrefix = "Вы: "
    override val pinChat = "Закрепить"
    override val unpinChat = "Открепить"
    override val mute = "Отключить звук"
    override val unmute = "Включить звук"
    override val archive = "В архив"
    override val unarchive = "Из архива"
    override val markUnread = "Отметить непрочитанным"
    override val markRead = "Отметить прочитанным"
    override val deleteChat = "Удалить чат"
    override val deleteChatConfirm = "Удалить чат с %s?"
    override val delete = "Удалить"
    override val cancel = "Отмена"
    override val messageHint = "Сообщение"
    override val botStartButton = "ЗАПУСТИТЬ (/start)"
    override val botMenuButtonDefault = "Меню"
    override val botCommandsTitle = "Команды бота"
    override val pinnedMessage = "Закреплённое сообщение"
    override val noMessagesYet = "Здесь пока нет сообщений…"
    override val typing = "печатает…"
    override val recordingVoice = "записывает голосовое…"
    override val sendingMedia = "отправляет медиа…"
    override val online = "в сети"
    override val lastSeenRecently = "был(а) недавно"
    override val lastSeenWithinWeek = "был(а) на этой неделе"
    override val lastSeenWithinMonth = "был(а) в этом месяце"
    override val lastSeenJustNow = "был(а) только что"
    override val lastSeenAt = "был(а) в %s"
    override val offline = "не в сети"
    override val subscribersCount = "%d подписчиков"
    override val membersCount = "%d участников"
    override val cloudStorage = "облачное хранилище"
    override val joinChannel = "ПОДПИСАТЬСЯ"
    override val leaveChannel = "Покинуть канал"
    override val leaveGroup = "Покинуть группу"
    override val reply = "Ответить"
    override val forward = "Переслать"
    override val copy = "Копировать"
    override val edit = "Изменить"
    override val pin = "Закрепить"
    override val unpin = "Открепить"
    override val editedLabel = "изм."
    override val forwardedFrom = "Переслано от "
    override val editMessageTitle = "Редактировать"
    override val replyToPrefix = "Ответ: "
    override val gallery = "Галерея"
    override val fileLabel = "Файл"
    override val forwardTo = "Переслать…"
    override val deleteMessageTitle = "Удалить сообщение"
    override val deleteMessageConfirm = "Удалить это сообщение?"
    override val deleteForEveryone = "Удалить для всех"
    override val clearHistory = "Очистить историю"
    override val infoAndMedia = "Профиль и медиа"
    override val micPermissionRequired = "Для записи голосовых сообщений нужен доступ к микрофону."
    override val today = "Сегодня"
    override val yesterday = "Вчера"
    override val openingWebApp = "Запуск мини-приложения…"
    override val webAppClose = "Закрыть"
    override val openInBrowser = "Открыть в браузере"
    override val reload = "Обновить"
    override val searchContactsHint = "Поиск контактов"
    override val sortByLastSeen = "По времени захода"
    override val sortByName = "По имени"
    override val newGroup = "Создать группу"
    override val newChannel = "Создать канал"
    override val newSecretChat = "Секретный чат"
    override val groupNameHint = "Название группы"
    override val channelNameHint = "Название канала"
    override val channelDescHint = "Описание (необязательно)"
    override val channelCreateSubtitle = "Создайте публичный или частный канал для неограниченной аудитории."
    override val createAction = "Создать"
    override val noContactsFound = "Контакты не найдены"
    override val setPhoto = "Фото"
    override val editInfo = "Изменить"
    override val infoSection = "Информация"
    override val mobileLabel = "Телефон"
    override val usernameLabel = "Имя пользователя"
    override val birthdateLabel = "День рождения"
    override val bioLabel = "О себе"
    override val personalChannel = "Личный канал"
    override val myQrCode = "Мой QR-код"
    override val postsTab = "Посты"
    override val savedTab = "Избранное"
    override val giftsTab = "Подарки"
    override val accountsSection = "Аккаунты"
    override val addAccount = "Детали аккаунта"
    override val accountTitle = "Аккаунт"
    override val accountSubtitle = "Номер, имя пользователя, о себе"
    override val chatSettingsTitle = "Настройки чатов"
    override val chatSettingsSubtitle = "Фон, тёмная тема, анимации"
    override val privacyTitle = "Конфиденциальность"
    override val privacySubtitle = "Сеансы, код-пароль, приватность"
    override val notificationsTitle = "Уведомления"
    override val notificationsSubtitle = "Звуки, звонки, счётчики"
    override val dataStorageTitle = "Данные и память"
    override val dataStorageSubtitle = "Загрузка медиа и кэш"
    override val chatFoldersTitle = "Папки с чатами"
    override val chatFoldersSubtitle = "Сортировка чатов по папкам"
    override val devicesTitle = "Устройства"
    override val devicesSubtitle = "Управление активными сеансами"
    override val powerSavingTitle = "Энергосбережение"
    override val powerSavingSubtitle = "Анимации и экономия заряда"
    override val languageTitle = "Язык"
    override val logOut = "Выйти"
    override val logOutConfirm = "Вы уверены, что хотите выйти из текущей сессии Telegram?"
    override val yourNameSection = "Ваше имя"
    override val bioHint = "О себе"
    override val bioHelper = "Любые подробности: возраст, род занятий или город."
    override val yourInfoSection = "Информация"
    override val tapToChangeUsername = "Изменить имя пользователя"
    override val tapToSetBirthdate = "Указать дату рождения"
    override val saveAction = "Сохранить"
    override val savedToast = "Сохранено"
    override val messageTextSize = "Размер текста сообщений"
    override val changeChatWallpaper = "Изменить фон чатов"
    override val changeNameColor = "Изменить цвет акцента"
    override val colorThemeSection = "Цветовая тема"
    override val switchToDayMode = "Дневной режим"
    override val switchToNightMode = "Ночной режим"
    override val messageCorners = "Углы сообщений"
    override val chatListView = "Список чатов"
    override val twoLineView = "Двухстрочный"
    override val threeLineView = "Трёхстрочный"
    override val swipeGestureTitle = "Жест смахивания в списке чатов"
    override val swipeDelete = "Удалить"
    override val swipeArchive = "В архив"
    override val swipePin = "Закрепить"
    override val swipeFolder = "Непрочитано"
    override val autoNightMode = "Автоматическая ночная тема"
    override val inAppBrowser = "Встроенный браузер"
    override val inAppBrowserDesc = "Открывать ссылки и мини-приложения внутри"
    override val animationsLabel = "Анимации"
    override val sendWithEnter = "Отправка клавишей Enter"
    override val securitySection = "Безопасность"
    override val passcodeLock = "Код-пароль"
    override val passcodeEnabled = "Вкл"
    override val passcodeDisabled = "Выкл"
    override val enterPasscodeTitle = "Введите код-пароль"
    override val setPasscodeHint = "Введите 4-значный PIN (пусто — отключить)"
    override val blockedUsers = "Чёрный список"
    override val privacySection = "Приватность"
    override val phonePrivacy = "Номер телефона"
    override val lastSeenPrivacy = "Время захода"
    override val photoPrivacy = "Фотографии профиля"
    override val forwardPrivacy = "Пересылка сообщений"
    override val callsPrivacy = "Звонки"
    override val bioPrivacy = "О себе"
    override val everybody = "Все"
    override val myContacts = "Мои контакты"
    override val nobody = "Никто"
    override val deleteAccountIfAway = "Удалить аккаунт, если не захожу"
    override val monthsFormat = "%d мес."
    override val privateChats = "Личные чаты"
    override val groupsLabel = "Группы"
    override val channelsLabel = "Каналы"
    override val messagePreview = "Предпросмотр текста"
    override val clearCache = "Очистить локальный кэш"
    override val cacheCleared = "Кэш очищен"
    override val totalCacheUsed = "Занято: %s"
    override val foldersHeroDesc = "Создавайте папки для разных чатов и быстро переключайтесь между ними."
    override val recommendedFolders = "Рекомендуемые папки"
    override val addFolderAction = "Добавить"
    override val createNewFolder = "Создать новую папку"
    override val folderNameHint = "Название папки"
    override val folderDisplayStyle = "Вид вкладок папок"
    override val showIconStyle = "Показывать значки"
    override val showNameStyle = "Показывать названия"
    override val unreadFolderName = "Непрочитанные"
    override val unreadFolderDesc = "Новые сообщения из всех чатов."
    override val personalFolderName = "Личные"
    override val personalFolderDesc = "Только личные переписки."
    override val devicesHeroDesc = "Активные устройства и сеансы Telegram этого аккаунта."
    override val thisDeviceSection = "Это устройство"
    override val terminateAllOtherSessions = "Завершить все другие сеансы"
    override val activeSessionsSection = "Активные сеансы"
    override val sessionTerminated = "Сеанс завершён"
    override val allOtherSessionsTerminated = "Все остальные сеансы завершены"
    override val powerSavingHero = "Уменьшение анимаций интерфейса при экономии заряда батареи."
    override val batteryThreshold = "Порог заряда"
    override val smoothAnimations = "Плавные анимации"
    override val syncContacts = "Синхронизировать контакты"
    override val addNewAccount = "Добавить аккаунт"
    override val slideToCancel = "Влево — отмена"
    override val holdForVideoTapForVoice = "Удерживайте для записи видео.\nНажмите для переключения на аудио."
    override val holdForVoiceTapForVideo = "Удерживайте для записи голоса.\nНажмите для переключения на видео."
    override val cameraPermissionRequired = "Для видеосообщений нужен доступ к камере и микрофону."
    override val musicLabel = "Музыка"
    override val pollLabel = "Опрос"
    override val locationLabel = "Геопозиция"
    override val contactLabel = "Контакт"
    override val createPollTitle = "Новый опрос"
    override val pollQuestionHint = "Вопрос"
    override val pollOptionHint = "Вариант %d"
    override val anonymousPoll = "Анонимное голосование"
    override val multipleAnswers = "Выбор нескольких ответов"
    override val tapToDownload = "Нажмите для загрузки"
    override val publicChannel = "публичный канал"
    override val privateChannel = "частный канал"
    override val publicGroup = "публичная группа"
    override val privateGroup = "частная группа"
    override val liveStream = "Трансляция"
    override val videoChat = "Видеочат"
    override val discussion = "Обсуждение"
    override val descriptionLabel = "Описание"
    override val inviteLinkLabel = "Пригласительная ссылка"
    override val subscribersLabel = "Подписчики"
    override val adminsLabel = "Администраторы"
    override val bannedLabel = "Чёрный список"
    override val membersLabel = "Участники"
    override val channelSettings = "Настройки канала"
    override val groupSettings = "Настройки группы"
    override val addMembers = "Добавить участников"
    override val ownerBadge = "Владелец"
    override val adminBadge = "Админ"
    override val setNewPhoto = "Выбрать фотографию"
    override val channelTypeLabel = "Тип канала"
    override val groupTypeLabel = "Тип группы"
    override val linkedChannelLabel = "Связанный канал"
    override val reactionsLabel = "Реакции"
    override val permissionsLabel = "Разрешения"
    override val deleteChannel = "Удалить канал"
    override val deleteAndLeaveGroup = "Удалить и покинуть группу"
    override val publicLabel = "Публичный"
    override val privateLabel = "Частный"
    override val storageAndNetworkSection = "Использование памяти и сети"
    override val storageUsageTitle = "Использование памяти"
    override val networkUsageTitle = "Использование трафика"
    override val saveLocationTitle = "Путь сохранения"
    override val internalStorage = "Внутренняя память"
    override val autoMediaDownloadSection = "Автозагрузка медиа"
    override val whenUsingMobile = "Через мобильную сеть"
    override val whenUsingWifi = "Через Wi-Fi"
    override val whenUsingRoaming = "В роуминге"
    override val notEnabled = "Отключено"
    override val enabledLabel = "Включено"
    override val resetAutoDownload = "Сбросить настройки автозагрузки"
    override val saveToGallerySection = "Сохранять в галерею"
    override val offLabel = "Выкл"
    override val streamingSection = "Стриминг"
    override val streamVideoAudio = "Стриминг видео и аудио"
    override val streamVideoAudioDesc = "Воспроизводить видео и музыку сразу без ожидания полной загрузки."
    override val callsSection = "Звонки"
    override val useLessData = "Экономия трафика"
    override val onlyWhileRoaming = "Только в роуминге"
    override val proxySection = "Прокси"
    override val proxySettings = "Настройки прокси"
    override val clearDrafts = "Удалить все черновики"
    override val clearCacheBtn = "Очистить кэш %s"
    override val storageCloudNote = "Все медиафайлы остаются в облаке Telegram и их можно скачать повторно в любой момент."
    override val allTab = "Все"
    override val mobileTab = "Мобильная"
    override val wifiTab = "Wi-Fi"
    override val roamingTab = "Роуминг"
    override val chatsTab = "Чаты"
    override val mediaTab = "Медиа"
    override val filesTab = "Файлы"
    override val musicTab = "Музыка"
    override val totalNetworkUsage = "Всего использовано трафика"
    override val sentData = "Отправлено"
    override val receivedData = "Получено"
    override val resetStatistics = "Сбросить статистику"
    override val stickersAndEmojis = "Стикеры и эмодзи"
    override val videosLabel = "Видео"
    override val photosLabel = "Фото"
    override val documentsLabel = "Документы"
    override val voiceMessagesLabel = "Голосовые"
    override val profilePhotosLabel = "Фото профиля"
    override val otherLabel = "Другое"
    override val messagesLabel = "Сообщения"
    override val previewPhoto = "Фото"
    override val previewVideo = "Видео"
    override val previewVoice = "Голосовое сообщение"
    override val previewSticker = "Стикер"
    override val previewVideoNote = "Видеосообщение"
    override val previewLocation = "Геопозиция"
    override val previewContact = "Контакт"
    override val previewVoiceCall = "Аудиозвонок"
    override val previewVideoCall = "Видеозвонок"
    override val previewMessage = "Сообщение"
    override val myStory = "Моя история"
    override val storiesLabel = "Истории"
    override val addPost = "Добавить пост"
    override val archivedPostsTab = "Архив постов"
    override val archivedStoriesHint = "Архивные истории видны только вам, пока вы не опубликуете их в профиле."
    override val postToProfile = "В профиль"
    override val archiveStory = "В архив"
    override val deleteStory = "Удалить историю"
    override val noStoriesYet = "Пока нет постов"
    override val allGifts = "Все подарки"
    override val addCollection = "+ Добавить коллекцию"
    override val giftsTapHint = "Нажмите на подарок, чтобы обменять его на звёзды или изменить настройки приватности."
    override val sendGiftsToFriends = "Отправить подарки друзьям"
    override val noGiftsYet = "Пока нет подарков"
    override val convertToStars = "Обменять на %d ⭐"
    override val hideFromProfile = "Скрыть из профиля"
    override val showOnProfile = "Показать в профиле"
    override val senderAnonymous = "Анонимно"
    override val sendGiftTitle = "Отправить подарок"
    override val telegramPremiumTitle = "Telegram Premium"
    override val telegramStarsTitle = "Звёзды Telegram"
    override val helpSection = "Помощь"
    override val askQuestion = "Задать вопрос"
    override val telegramFaq = "Вопросы о Telegram"
    override val telegramFeatures = "Возможности Telegram"
    override val privacyPolicy = "Политика конфиденциальности"
    override val askVolunteerTitle = "Задать вопрос"
    override val askVolunteerBody = "Помните, что служба поддержки Ghostly Messenger работает силами волонтёров. Мы постараемся ответить как можно быстрее, но это может занять некоторое время.\n\nПосмотрите раздел вопросов о Telegram: там есть ответы на многие вопросы и советы по устранению неполадок."
    override val askVolunteerBtn = "Спросить волонтёра"
    override val supportCenterTitle = "Центр поддержки"
    override val supportRoleSubtitle = "поддержка"
    override val supportPhoneDisplay = "+47777"
    override val supportBioDisplay = "Этот аккаунт ведётся официальной командой Ghostly Messenger"
    override val supportWelcomePill = "Есть вопрос о\nGhostly Messenger?"
    override val addToContacts = "Добавить в контакты"
    override val supportAdminPanel = "Панель админа"
    override val yourBalance = "ваш баланс"
    override val earnStars = "Заработать звёзды"
    override val earnStarsDesc = "Делитесь ссылками на мини-приложения и получайте долю их дохода в звёздах."
    override val allTransactionsTab = "Все транзакции"
    override val incomingTab = "Входящие"
    override val outgoingTab = "Исходящие"
    override val giftConverted = "🧸 Подарок обменян"
    override val starReaction = "Звёздная реакция"
    override val topUpBalance = "Пополнить баланс"
    override val appFooterVersion = "Ghostly Messenger for Android v1.0.0\nstore bundled arm64-v8a"
    override val customEmojiPackTitle = "Набор эмодзи"
    override val addEmojiPack = "Добавить набор"
    override val emojiPackAdded = "Набор добавлен"
    override val callLabel = "Позвонить"
    override val ageYearsFormat = "(%d лет)"
}

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.UZ -> UzbekStrings
    AppLanguage.EN -> EnglishStrings
    AppLanguage.RU -> RussianStrings
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { UzbekStrings }

val AppStrings.googleSignInGateTitle: String
    get() = when (this) {
        UzbekStrings -> "Ghostly Messenger Bulut Xizmati"
        EnglishStrings -> "Ghostly Messenger Cloud Sync"
        else -> "Облачный сервис Ghostly Messenger"
    }

val AppStrings.googleSignInGateDesc: String
    get() = when (this) {
        UzbekStrings -> "Hisob, yordam markazi (+47777) va sozlamalarni sinxronlash uchun Google hisobingiz bilan kiring."
        EnglishStrings -> "Sign in with your Google account to sync your profile, support center (+47777), and settings."
        else -> "Войдите через Google, чтобы синхронизировать профиль, службу поддержки (+47777) и настройки."
    }

val AppStrings.googleSignInBtn: String
    get() = when (this) {
        UzbekStrings -> "Google bilan kirish"
        EnglishStrings -> "Sign in with Google"
        else -> "Войти через Google"
    }

val AppStrings.adminPanelTitle: String
    get() = when (this) {
        UzbekStrings -> "Admin"
        EnglishStrings -> "Admin"
        else -> "Админ"
    }

val AppStrings.adminStatsUsers: String
    get() = when (this) {
        UzbekStrings -> "Foydalanuvchilar"
        EnglishStrings -> "Users"
        else -> "Пользователи"
    }

val AppStrings.adminStatsSpammed: String
    get() = when (this) {
        UzbekStrings -> "Spamda"
        EnglishStrings -> "Spammed"
        else -> "В спаме"
    }

val AppStrings.adminStatsMessages: String
    get() = when (this) {
        UzbekStrings -> "Xabarlar"
        EnglishStrings -> "Messages"
        else -> "Сообщения"
    }

val AppStrings.adminTabUsers: String
    get() = when (this) {
        UzbekStrings -> "Foydalanuvchilar"
        EnglishStrings -> "Users"
        else -> "Пользователи"
    }

val AppStrings.adminTabMessages: String
    get() = when (this) {
        UzbekStrings -> "Xabarlar"
        EnglishStrings -> "Messages"
        else -> "Сообщения"
    }

val AppStrings.adminTabChannel: String
    get() = when (this) {
        UzbekStrings -> "Ghostly kanali"
        EnglishStrings -> "Ghostly Channel"
        else -> "Канал Ghostly"
    }

val AppStrings.adminSearchUsersHint: String
    get() = when (this) {
        UzbekStrings -> "Ism, @username, ID yoki raqam boʻyicha qidirish"
        EnglishStrings -> "Search by name, @username, ID or phone"
        else -> "Поиск по имени, @username, ID или номеру"
    }

val AppStrings.adminNoUsersFound: String
    get() = when (this) {
        UzbekStrings -> "Foydalanuvchilar topilmadi"
        EnglishStrings -> "No users found"
        else -> "Пользователи не найдены"
    }

val AppStrings.adminSpam24h: String
    get() = when (this) {
        UzbekStrings -> "Spam deb belgilash (24 soat)"
        EnglishStrings -> "Mark as Spam (24 hours)"
        else -> "Отметить как спам (24 часа)"
    }

val AppStrings.adminUnspamNow: String
    get() = when (this) {
        UzbekStrings -> "Spamdan olib tashlash"
        EnglishStrings -> "Remove Spam Restriction"
        else -> "Снять ограничение спама"
    }

val AppStrings.adminViewProfile: String
    get() = when (this) {
        UzbekStrings -> "Profilini koʻrish"
        EnglishStrings -> "View Profile"
        else -> "Открыть профиль"
    }

val AppStrings.adminSpamActiveBadge: String
    get() = when (this) {
        UzbekStrings -> "SPAM"
        EnglishStrings -> "SPAM"
        else -> "СПАМ"
    }

val AppStrings.adminConnectedChannels: String
    get() = when (this) {
        UzbekStrings -> "Ulangan kanallar va guruhlar"
        EnglishStrings -> "Connected Channels & Groups"
        else -> "Подключённые каналы и группы"
    }

val AppStrings.adminNoChannels: String
    get() = when (this) {
        UzbekStrings -> "Ulangan kanal yoki guruhlar topilmadi"
        EnglishStrings -> "No connected channels or groups"
        else -> "Нет подключённых каналов или групп"
    }

val AppStrings.adminGhostlyChannelDesc: String
    get() = when (this) {
        UzbekStrings -> "Ilovadan kirgan barcha foydalanuvchilar ushbu kanalga avtomatik qoʻshiladi. Chiqib ketsa ham 5 daqiqa ichida qayta qoʻshiladi."
        EnglishStrings -> "All app users automatically join this channel on login and are re-joined within 5 minutes if they leave."
        else -> "Все пользователи приложения автоматически подписываются на этот канал и возвращаются в течение 5 минут при выходе."
    }

val AppStrings.adminChannelLinkLabel: String
    get() = when (this) {
        UzbekStrings -> "Kanal havolasi (https://t.me/... yoki @username)"
        EnglishStrings -> "Channel link (https://t.me/... or @username)"
        else -> "Ссылка на канал (https://t.me/... или @username)"
    }

val AppStrings.adminChannelEnabledLabel: String
    get() = when (this) {
        UzbekStrings -> "Avtomatik qoʻshish yoqilgan"
        EnglishStrings -> "Auto-join enabled"
        else -> "Автоподписка включена"
    }

val AppStrings.adminConfirmBtn: String
    get() = when (this) {
        UzbekStrings -> "Tasdiqlash"
        EnglishStrings -> "Confirm"
        else -> "Подтвердить"
    }

val AppStrings.adminNoSupportThreads: String
    get() = when (this) {
        UzbekStrings -> "Hozircha +47777 ga yozilgan xabarlar yoʻq"
        EnglishStrings -> "No messages sent to +47777 yet"
        else -> "Сообщений в поддержку +47777 пока нет"
    }

val AppStrings.adminReplyHint: String
    get() = when (this) {
        UzbekStrings -> "Foydalanuvchiga javob yozing…"
        EnglishStrings -> "Write a reply to the user…"
        else -> "Напишите ответ пользователю…"
    }

val AppStrings.spamRestrictedTitle: String
    get() = when (this) {
        UzbekStrings -> "Siz spamga tushgansiz"
        EnglishStrings -> "Spam Restriction Active"
        else -> "Ограничение за спам"
    }

val AppStrings.spamRestrictedBanner: String
    get() = when (this) {
        UzbekStrings -> "Sizda spam cheklovi bor. Boshqalarga xabar yuborish taqiqlangan (%s qoldi)."
        EnglishStrings -> "You are marked as spam. Messaging others is disabled (%s left)."
        else -> "Вы отмечены как спам. Отправка сообщений ограничена (осталось %s)."
    }

val AppStrings.spamRestrictedDialogBody: String
    get() = when (this) {
        UzbekStrings -> "Hisobingizga spam cheklovi qoʻyilganligi sababli boshqalarga xabar yubora olmaysiz. Cheklov %s dan soʻng avtomatik olib tashlanadi."
        EnglishStrings -> "Your account currently has a spam restriction and cannot send messages to others. It will be lifted automatically in %s."
        else -> "На ваш аккаунт наложено ограничение за спам — вы не можете отправлять сообщения другим пользователям. Ограничение будет снято автоматически через %s."
    }

val AppStrings.userIdLabel: String
    get() = when (this) {
        UzbekStrings -> "Telegram ID"
        EnglishStrings -> "Telegram ID"
        else -> "Telegram ID"
    }

val AppStrings.adminAllChatsTab: String
    get() = when (this) {
        UzbekStrings -> "Barchasi"
        EnglishStrings -> "All"
        else -> "Все"
    }

val AppStrings.adminPrivateChatsTab: String
    get() = when (this) {
        UzbekStrings -> "Shaxsiy"
        EnglishStrings -> "Private"
        else -> "Личные"
    }

val AppStrings.adminGroupsTab: String
    get() = when (this) {
        UzbekStrings -> "Guruhlar"
        EnglishStrings -> "Groups"
        else -> "Группы"
    }

val AppStrings.adminChannelsTab: String
    get() = when (this) {
        UzbekStrings -> "Kanallar"
        EnglishStrings -> "Channels"
        else -> "Каналы"
    }

val AppStrings.adminBotsTab: String
    get() = when (this) {
        UzbekStrings -> "Botlar"
        EnglishStrings -> "Bots"
        else -> "Боты"
    }

val AppStrings.adminAllUserChats: String
    get() = when (this) {
        UzbekStrings -> "Barcha chatlari"
        EnglishStrings -> "All Chats"
        else -> "Все чаты"
    }

val AppStrings.adminSearchUserChatsHint: String
    get() = when (this) {
        UzbekStrings -> "Chat, kanal yoki xabar boʻyicha qidirish"
        EnglishStrings -> "Search chats, channels or messages"
        else -> "Поиск чатов, каналов или сообщений"
    }

val AppStrings.adminNoUserChats: String
    get() = when (this) {
        UzbekStrings -> "Bu boʻlimda chatlar topilmadi"
        EnglishStrings -> "No chats found in this category"
        else -> "Чаты в этом разделе не найдены"
    }

val AppStrings.adminChatDetailsTitle: String
    get() = when (this) {
        UzbekStrings -> "Chat maʼlumotlari"
        EnglishStrings -> "Chat Details"
        else -> "Информация о чате"
    }

val AppStrings.adminIncomingMessagesTitle: String
    get() = when (this) {
        UzbekStrings -> "Kelgan va oxirgi xabarlar"
        EnglishStrings -> "Incoming & Recent Messages"
        else -> "Входящие и последние сообщения"
    }

val AppStrings.adminNoChatMessages: String
    get() = when (this) {
        UzbekStrings -> "Bu chatda hozircha xabarlar topilmadi"
        EnglishStrings -> "No messages available in this chat yet"
        else -> "Сообщений в этом чате пока нет"
    }

val AppStrings.adminEnterChatBtn: String
    get() = when (this) {
        UzbekStrings -> "Chatga kirish"
        EnglishStrings -> "Enter Chat"
        else -> "Войти в чат"
    }

val AppStrings.adminChatOpenFailed: String
    get() = when (this) {
        UzbekStrings -> "Ushbu chatni joriy qurilmada ochib boʻlmadi, lekin maʼlumot va xabarlar yuqorida koʻrsatildi"
        EnglishStrings -> "Could not open this chat directly on this session; synced details and messages are shown above"
        else -> "Не удалось открыть чат в текущей сессии; данные и сообщения показаны выше"
    }

val AppStrings.adminSupportMessagesWithUser: String
    get() = when (this) {
        UzbekStrings -> "+47777 kelgan xabarlarni koʻrish"
        EnglishStrings -> "View +47777 Messages"
        else -> "Сообщения +47777"
    }

val AppStrings.adminChatTypeLabel: String
    get() = when (this) {
        UzbekStrings -> "Turi"
        EnglishStrings -> "Type"
        else -> "Тип"
    }

val AppStrings.adminMembersCountLabel: String
    get() = when (this) {
        UzbekStrings -> "Aʼzolar"
        EnglishStrings -> "Members"
        else -> "Участники"
    }

val AppStrings.adminRoleLabel: String
    get() = when (this) {
        UzbekStrings -> "Holati / Roli"
        EnglishStrings -> "Role"
        else -> "Роль"
    }

val AppStrings.adminUnreadLabel: String
    get() = when (this) {
        UzbekStrings -> "Oʻqilmagan"
        EnglishStrings -> "Unread"
        else -> "Непрочитано"
    }

val AppStrings.adminIncomingTag: String
    get() = when (this) {
        UzbekStrings -> "Kelgan"
        EnglishStrings -> "Incoming"
        else -> "Входящее"
    }

val AppStrings.adminOutgoingTag: String
    get() = when (this) {
        UzbekStrings -> "Chiquvchi"
        EnglishStrings -> "Outgoing"
        else -> "Исходящее"
    }

val AppStrings.ghostModeTitle: String
    get() = when (this) {
        UzbekStrings -> "Arvoh rejimi"
        EnglishStrings -> "Ghost Mode"
        else -> "Режим призрака"
    }

val AppStrings.ghostModeOnToast: String
    get() = when (this) {
        UzbekStrings -> "Arvoh rejimi yoqildi — xabarlar oʻqilmagan holda koʻriladi"
        EnglishStrings -> "Ghost Mode on — messages stay unread for others"
        else -> "Режим призрака включён — сообщения остаются непрочитанными"
    }

val AppStrings.ghostModeOffToast: String
    get() = when (this) {
        UzbekStrings -> "Arvoh rejimi oʻchirildi"
        EnglishStrings -> "Ghost Mode off"
        else -> "Режим призрака выключен"
    }

val AppStrings.ghostModeActiveBadge: String
    get() = when (this) {
        UzbekStrings -> "Arvoh rejimi faol • Oʻqilganlik yashirin"
        EnglishStrings -> "Ghost Mode active • Read receipts hidden"
        else -> "Режим призрака • Прочтение скрыто"
    }

val AppStrings.recordingTooShortHint: String
    get() = when (this) {
        UzbekStrings -> "Yozish uchun bosib turing, yuborish uchun qoʻyib yuboring"
        EnglishStrings -> "Hold to record, release to send"
        else -> "Удерживайте для записи, отпустите для отправки"
    }


