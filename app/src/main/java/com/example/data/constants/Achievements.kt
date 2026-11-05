package com.example.data.constants

data class AchievementTemplate(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val category: String,
    val target: Int
)

val ALL_ACHIEVEMENTS = listOf(

    // ═══ HABITS (20) ═══
    AchievementTemplate("FIRST_HABIT", "متميز", "أضفت أول عادة", "star", "HABITS", 1),
    AchievementTemplate("HABITS_5", "خمسة أعمدة", "أضفت 5 عادات", "star", "HABITS", 5),
    AchievementTemplate("HABITS_10", "عشر عادات", "أضفت 10 عادات", "star", "HABITS", 10),
    AchievementTemplate("HABITS_20", "عشرون عادة", "أضفت 20 عادة", "star", "HABITS", 20),
    AchievementTemplate("HABIT_STREAK_3", "البداية الثلاثية", "3 أيام متتالية", "local_fire_department", "HABITS", 3),
    AchievementTemplate("HABIT_STREAK_7", "أسبوع كامل", "7 أيام متتالية", "local_fire_department", "HABITS", 7),
    AchievementTemplate("HABIT_STREAK_14", "أسبوعان", "14 يوم متتالي", "local_fire_department", "HABITS", 14),
    AchievementTemplate("HABIT_STREAK_30", "شهر كامل", "30 يوم متتالي", "local_fire_department", "HABITS", 30),
    AchievementTemplate("HABIT_STREAK_60", "شهران", "60 يوم متتالي", "local_fire_department", "HABITS", 60),
    AchievementTemplate("HABIT_STREAK_100", "مئة يوم", "100 يوم متتالي", "local_fire_department", "HABITS", 100),
    AchievementTemplate("HABIT_STREAK_365", "سنة كاملة", "365 يوم متتالي", "local_fire_department", "HABITS", 365),
    AchievementTemplate("PERFECT_DAY", "يوم مثالي", "أكملت كل العادات في يوم", "verified", "HABITS", 1),
    AchievementTemplate("PERFECT_WEEK", "أسبوع مثالي", "7 أيام بكل العادات", "verified", "HABITS", 7),
    AchievementTemplate("PERFECT_MONTH", "شهر مثالي", "30 يوم بكل العادات", "verified", "HABITS", 30),
    AchievementTemplate("HABITS_50_COMPLETED", "خمسون إنجاز", "أكملت 50 عادة", "task_alt", "HABITS", 50),
    AchievementTemplate("HABITS_100_COMPLETED", "مئة إنجاز", "أكملت 100 عادة", "task_alt", "HABITS", 100),
    AchievementTemplate("HABITS_500_COMPLETED", "خمسمئة إنجاز", "أكملت 500 عادة", "task_alt", "HABITS", 500),
    AchievementTemplate("HABITS_1000_COMPLETED", "ألف إنجاز", "أكملت 1000 عادة", "task_alt", "HABITS", 1000),
    AchievementTemplate("EARLY_BIRD", "طائر الصباح", "أكملت عادة قبل 7 صباحاً", "wb_sunny", "HABITS", 1),
    AchievementTemplate("HABIT_MASTER", "سيد العادات", "5000 عادة منجزة", "workspace_premium", "HABITS", 5000),

    // ═══ FITNESS (20) ═══
    AchievementTemplate("FIRST_WORKOUT", "أول تمرين", "أول جلسة مشي", "directions_walk", "FITNESS", 1),
    AchievementTemplate("STEPS_1K", "ألف خطوة", "1000 خطوة في يوم", "directions_walk", "FITNESS", 1000),
    AchievementTemplate("STEPS_5K", "خمسة آلاف", "5000 خطوة في يوم", "directions_walk", "FITNESS", 5000),
    AchievementTemplate("STEPS_10K", "عشرة آلاف", "10000 خطوة في يوم", "directions_walk", "FITNESS", 10000),
    AchievementTemplate("STEPS_20K", "عشرون ألفاً", "20000 خطوة في يوم", "directions_walk", "FITNESS", 20000),
    AchievementTemplate("STEPS_30K", "يوم خارق", "30000 خطوة في يوم", "directions_walk", "FITNESS", 30000),
    AchievementTemplate("STEPS_50K", "خمسون ألفاً", "50000 خطوة في يوم", "directions_walk", "FITNESS", 50000),
    AchievementTemplate("STEP_TOTAL_100K", "مئة ألف", "100,000 خطوة تراكمياً", "route", "FITNESS", 100000),
    AchievementTemplate("STEP_TOTAL_500K", "نصف مليون", "500,000 خطوة تراكمياً", "route", "FITNESS", 500000),
    AchievementTemplate("STEP_TOTAL_1M", "مليون خطوة", "1,000,000 خطوة تراكمياً", "route", "FITNESS", 1000000),
    AchievementTemplate("STEP_TOTAL_5M", "خمسة ملايين", "5,000,000 خطوة تراكمياً", "route", "FITNESS", 5000000),
    AchievementTemplate("WATER_8", "ريّان", "8 أكواب في يوم", "water_drop", "FITNESS", 8),
    AchievementTemplate("WATER_WEEK", "ترطيب أسبوعي", "7 أيام × 8 أكواب", "water_drop", "FITNESS", 7),
    AchievementTemplate("WATER_MONTH", "ترطيب شهري", "30 يوم × 8 أكواب", "water_drop", "FITNESS", 30),
    AchievementTemplate("WORKOUT_10", "عشرة تمارين", "10 جلسات مشي", "fitness_center", "FITNESS", 10),
    AchievementTemplate("WORKOUT_50", "خمسون تمريناً", "50 جلسة مشي", "fitness_center", "FITNESS", 50),
    AchievementTemplate("WORKOUT_100", "مئة تمرين", "100 جلسة مشي", "fitness_center", "FITNESS", 100),
    AchievementTemplate("WORKOUT_500", "خمسمئة تمرين", "500 جلسة مشي", "fitness_center", "FITNESS", 500),
    AchievementTemplate("WALK_STREAK_7", "أسبوع مشي", "7 أيام مشي متتالية", "local_fire_department", "FITNESS", 7),
    AchievementTemplate("WALK_STREAK_30", "شهر مشي", "30 يوم مشي متتالي", "local_fire_department", "FITNESS", 30),

    // ═══ STUDY (20) ═══
    AchievementTemplate("FIRST_STUDY", "أول جلسة", "أول جلسة دراسة", "school", "STUDY", 1),
    AchievementTemplate("STUDY_10H", "10 ساعات", "10 ساعات دراسة", "school", "STUDY", 600),
    AchievementTemplate("STUDY_50H", "50 ساعة", "50 ساعة دراسة", "school", "STUDY", 3000),
    AchievementTemplate("STUDY_100H", "100 ساعة", "100 ساعة دراسة", "school", "STUDY", 6000),
    AchievementTemplate("STUDY_500H", "500 ساعة", "500 ساعة دراسة", "school", "STUDY", 30000),
    AchievementTemplate("STUDY_1000H", "1000 ساعة", "1000 ساعة دراسة", "school", "STUDY", 60000),
    AchievementTemplate("POMODORO_10", "10 بومودورو", "10 جلسات بومودورو", "timer", "STUDY", 10),
    AchievementTemplate("POMODORO_50", "50 بومودورو", "50 جلسة", "timer", "STUDY", 50),
    AchievementTemplate("POMODORO_100", "100 بومودورو", "100 جلسة", "timer", "STUDY", 100),
    AchievementTemplate("POMODORO_500", "500 بومودورو", "500 جلسة", "timer", "STUDY", 500),
    AchievementTemplate("POMODORO_1000", "1000 بومودورو", "1000 جلسة", "timer", "STUDY", 1000),
    AchievementTemplate("FOCUS_PERFECT", "تركيز مثالي", "تقييم تركيز 10/10", "psychology", "STUDY", 1),
    AchievementTemplate("EARLY_STUDY", "دراسة الفجر", "دراسة قبل 7 صباحاً", "wb_twilight", "STUDY", 1),
    AchievementTemplate("LATE_STUDY", "دراسة الليل", "دراسة بعد 10 مساءً", "nightlight", "STUDY", 1),
    AchievementTemplate("STUDY_STREAK_7", "أسبوع دراسة", "7 أيام متتالية", "local_fire_department", "STUDY", 7),
    AchievementTemplate("STUDY_STREAK_30", "شهر دراسة", "30 يوم متتالي", "local_fire_department", "STUDY", 30),
    AchievementTemplate("STUDY_STREAK_100", "100 يوم", "100 يوم دراسة", "local_fire_department", "STUDY", 100),
    AchievementTemplate("STUDY_STREAK_365", "سنة دراسة", "365 يوم دراسة", "local_fire_department", "STUDY", 365),
    AchievementTemplate("STUDY_5000H", "5000 ساعة", "5000 ساعة دراسة", "workspace_premium", "STUDY", 300000),
    AchievementTemplate("STUDY_MASTER", "سيد الدراسة", "10000 ساعة دراسة", "workspace_premium", "STUDY", 600000),

    // ═══ NUTRITION (15) ═══
    AchievementTemplate("FIRST_MEAL", "أول وجبة", "سجلت أول وجبة", "restaurant", "NUTRITION", 1),
    AchievementTemplate("MEALS_10", "عشر وجبات", "10 وجبات مسجلة", "restaurant", "NUTRITION", 10),
    AchievementTemplate("MEALS_50", "خمسون وجبة", "50 وجبة", "restaurant", "NUTRITION", 50),
    AchievementTemplate("MEALS_100", "مئة وجبة", "100 وجبة", "restaurant", "NUTRITION", 100),
    AchievementTemplate("MEALS_500", "خمسمئة وجبة", "500 وجبة", "restaurant", "NUTRITION", 500),
    AchievementTemplate("CALORIES_7D", "أسبوع سعرات", "7 أيام تتبع", "local_dining", "NUTRITION", 7),
    AchievementTemplate("CALORIES_30D", "شهر سعرات", "30 يوم تتبع", "local_dining", "NUTRITION", 30),
    AchievementTemplate("PROTEIN_100", "بروتين 100", "100 غ بروتين في يوم", "egg", "NUTRITION", 100),
    AchievementTemplate("HEALTHY_WEEK", "أسبوع صحي", "أسبوع أكل متوازن", "eco", "NUTRITION", 7),
    AchievementTemplate("HEALTHY_MONTH", "شهر صحي", "30 يوم أكل صحي", "eco", "NUTRITION", 30),
    AchievementTemplate("PERFECT_NUTRITION", "يوم تغذية مثالي", "يوم مثالي بالسعرات", "check_circle", "NUTRITION", 1),
    AchievementTemplate("NO_JUNK_7D", "بدون ضار", "7 أيام بدون أكل ضار", "block", "NUTRITION", 7),
    AchievementTemplate("NUTRITION_MASTER", "سيد التغذية", "1000 وجبة مسجلة", "workspace_premium", "NUTRITION", 1000),
    AchievementTemplate("CHEF", "الطاهي", "سجلت 50 وصفة", "soup_kitchen", "NUTRITION", 50),
    AchievementTemplate("WATER_TRACK_100", "ترطيب 100", "100 يوم تتبع الماء", "water_drop", "NUTRITION", 100),

    // ═══ GENERAL (25) ═══
    AchievementTemplate("APP_7_DAYS", "أسبوع مع MIND", "7 أيام استخدام", "event_available", "GENERAL", 7),
    AchievementTemplate("APP_30_DAYS", "شهر مع MIND", "30 يوم", "event_available", "GENERAL", 30),
    AchievementTemplate("APP_100_DAYS", "100 يوم", "100 يوم", "event_available", "GENERAL", 100),
    AchievementTemplate("APP_365_DAYS", "سنة كاملة", "365 يوم", "event_available", "GENERAL", 365),
    AchievementTemplate("MORNING_USER", "صباحي", "30 يوم صباحاً", "wb_sunny", "GENERAL", 30),
    AchievementTemplate("NIGHT_OWL", "ليلي", "30 يوم ليلاً", "nightlight", "GENERAL", 30),
    AchievementTemplate("WEEKEND_WARRIOR", "محارب الويكند", "20 ويكند نشيط", "weekend", "GENERAL", 20),
    AchievementTemplate("STREAK_KEEPER", "حافظ السلسلة", "30 يوم متواصل", "local_fire_department", "GENERAL", 30),
    AchievementTemplate("COLLECTOR", "جامع الإنجازات", "50 إنجاز", "collections_bookmark", "GENERAL", 50),
    AchievementTemplate("LEGEND", "أسطورة", "100 إنجاز", "workspace_premium", "GENERAL", 100),
    AchievementTemplate("FIRST_NOTE", "أول ملاحظة", "كتبت أول ملاحظة", "note", "GENERAL", 1),
    AchievementTemplate("NOTES_50", "خمسون ملاحظة", "50 ملاحظة", "note", "GENERAL", 50),
    AchievementTemplate("FIRST_JOURNAL", "أول مذكرة", "كتبت أول مذكرة يومية", "menu_book", "GENERAL", 1),
    AchievementTemplate("JOURNAL_7", "أسبوع مذكرات", "7 أيام مذكرات", "menu_book", "GENERAL", 7),
    AchievementTemplate("JOURNAL_30", "شهر مذكرات", "30 يوم مذكرات", "menu_book", "GENERAL", 30),
    AchievementTemplate("JOURNAL_100", "100 مذكرة", "100 يوم مذكرات", "menu_book", "GENERAL", 100),
    AchievementTemplate("PERFECT_ALL", "يوم شامل مثالي", "كل شيء مثالي في يوم", "emoji_events", "GENERAL", 1),
    AchievementTemplate("PERFECT_WEEK_ALL", "أسبوع شامل مثالي", "7 أيام مثالية", "emoji_events", "GENERAL", 7),
    AchievementTemplate("FIVE_HABITS_DAY", "خمس في يوم", "5 عادات في يوم", "stars", "GENERAL", 5),
    AchievementTemplate("MULTI_TASKER", "متعدد المهام", "5 ميزات في يوم", "apps", "GENERAL", 5),
    AchievementTemplate("EXPLORER", "المستكشف", "استخدمت كل الشاشات", "explore", "GENERAL", 1),
    AchievementTemplate("CONSISTENT", "المنتظم", "30 يوم بدون انقطاع", "schedule", "GENERAL", 30),
    AchievementTemplate("DEDICATED", "المخلص", "100 يوم بدون انقطاع", "military_tech", "GENERAL", 100),
    AchievementTemplate("CHAMPION", "البطل", "365 يوم بدون انقطاع", "emoji_events", "GENERAL", 365),
    AchievementTemplate("GRAND_MASTER", "الأستاذ", "1000 يوم مع التطبيق", "workspace_premium", "GENERAL", 1000)
)

fun categoryLabelAr(category: String): String = when (category) {
    "HABITS" -> "العادات"
    "FITNESS" -> "اللياقة"
    "STUDY" -> "الدراسة"
    "NUTRITION" -> "التغذية"
    "GENERAL" -> "عام"
    else -> "أخرى"
}
