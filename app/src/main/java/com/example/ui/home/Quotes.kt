package com.example.ui.home

import java.util.Calendar

data class Quote(val text: String, val author: String)

private val ARABIC_QUOTES = listOf(
    Quote("العلم في الصغر كالنقش على الحجر", "مثل عربي"),
    Quote("من جد وجد، ومن زرع حصد", "مثل عربي"),
    Quote("لا تؤجل عمل اليوم إلى الغد", "مثل عربي"),
    Quote("الوقت كالسيف إن لم تقطعه قطعك", "مثل عربي"),
    Quote("رحلة الألف ميل تبدأ بخطوة واحدة", "لاوتسي"),
    Quote("النجاح مجموع جهود صغيرة تتكرر يومياً", "روبرت كولي"),
    Quote("لا تنتظر الفرصة، اصنعها", "مجهول"),
    Quote("الانضباط هو جسر بين الأهداف والإنجازات", "جيم رون"),
    Quote("افعل شيئاً اليوم يجعلك تشكر نفسك غداً", "مجهول"),
    Quote("النجاح ليس نهائياً، والفشل ليس قاتلاً", "ونستون تشرشل"),
    Quote("أنت أقوى مما تظن، وأذكى مما تعتقد", "مجهول"),
    Quote("لا تقارن بدايتك بنهاية الآخرين", "مجهول"),
    Quote("الاستمرارية أهم من الكمال", "مجهول"),
    Quote("يومك يبدأ بقرارك", "مجهول"),
    Quote("الصحة تاج على رؤوس الأصحاء", "مثل عربي"),
    Quote("العقل السليم في الجسم السليم", "مثل لاتيني"),
    Quote("الوقت لا ينتظر أحداً", "مجهول"),
    Quote("كل يوم فرصة جديدة للبدء من جديد", "مجهول"),
    Quote("الطريق إلى النجاح مفروش بالإصرار", "مجهول"),
    Quote("اعمل بصمت، ودع نجاحك يتحدث", "مجهول"),
    Quote("لا تخف من الفشل، خف من ألا تحاول", "مجهول"),
    Quote("التميز ليس فعلاً، بل عادة", "أرسطو"),
    Quote("المعرفة قوة", "فرانسيس بيكون"),
    Quote("القراءة غذاء العقل", "مجهول"),
    Quote("من أراد العلا سهر الليالي", "مثل عربي"),
    Quote("العزيمة تصنع المعجزات", "مجهول"),
    Quote("التركيز على الهدف هو سر النجاح", "مجهول"),
    Quote("نظّم وقتك، تنظم حياتك", "مجهول"),
    Quote("الصبر مفتاح الفرج", "مثل عربي"),
    Quote("خطوة صغيرة كل يوم تصنع فرقاً كبيراً", "مجهول")
)

private val QUOTES_EN: Map<String, String> = mapOf(
    "العلم في الصغر كالنقش على الحجر" to "Learning in youth is like carving in stone",
    "من جد وجد، ومن زرع حصد" to "Whoever strives succeeds, and whoever sows reaps",
    "لا تؤجل عمل اليوم إلى الغد" to "Don't put off today's work until tomorrow",
    "الوقت كالسيف إن لم تقطعه قطعك" to "Time is like a sword: if you don't cut it, it cuts you",
    "رحلة الألف ميل تبدأ بخطوة واحدة" to "A journey of a thousand miles begins with a single step",
    "النجاح مجموع جهود صغيرة تتكرر يومياً" to "Success is the sum of small efforts repeated daily",
    "لا تنتظر الفرصة، اصنعها" to "Don't wait for opportunity, create it",
    "الانضباط هو جسر بين الأهداف والإنجازات" to "Discipline is the bridge between goals and accomplishment",
    "افعل شيئاً اليوم يجعلك تشكر نفسك غداً" to "Do something today that your future self will thank you for",
    "النجاح ليس نهائياً، والفشل ليس قاتلاً" to "Success is not final, failure is not fatal",
    "أنت أقوى مما تظن، وأذكى مما تعتقد" to "You are stronger than you think and smarter than you believe",
    "لا تقارن بدايتك بنهاية الآخرين" to "Don't compare your beginning to someone else's end",
    "الاستمرارية أهم من الكمال" to "Consistency matters more than perfection",
    "يومك يبدأ بقرارك" to "Your day begins with your decision",
    "الصحة تاج على رؤوس الأصحاء" to "Health is a crown on the heads of the healthy",
    "العقل السليم في الجسم السليم" to "A sound mind in a sound body",
    "الوقت لا ينتظر أحداً" to "Time waits for no one",
    "كل يوم فرصة جديدة للبدء من جديد" to "Every day is a new chance to start again",
    "الطريق إلى النجاح مفروش بالإصرار" to "The road to success is paved with persistence",
    "اعمل بصمت، ودع نجاحك يتحدث" to "Work in silence and let your success speak",
    "لا تخف من الفشل، خف من ألا تحاول" to "Don't fear failure, fear not trying",
    "التميز ليس فعلاً، بل عادة" to "Excellence is not an act, but a habit",
    "المعرفة قوة" to "Knowledge is power",
    "القراءة غذاء العقل" to "Reading is food for the mind",
    "من أراد العلا سهر الليالي" to "Whoever seeks greatness stays up through the nights",
    "العزيمة تصنع المعجزات" to "Determination creates miracles",
    "التركيز على الهدف هو سر النجاح" to "Focus on the goal is the secret of success",
    "نظّم وقتك، تنظم حياتك" to "Organize your time, organize your life",
    "الصبر مفتاح الفرج" to "Patience is the key to relief",
    "خطوة صغيرة كل يوم تصنع فرقاً كبيراً" to "A small step every day makes a big difference",
)

private val AUTHORS_EN: Map<String, String> = mapOf(
    "مثل عربي" to "Arabic proverb",
    "لاوتسي" to "Lao Tzu",
    "روبرت كولي" to "Robert Collier",
    "مجهول" to "Unknown",
    "جيم رون" to "Jim Rohn",
    "ونستون تشرشل" to "Winston Churchill",
    "مثل لاتيني" to "Latin proverb",
    "أرسطو" to "Aristotle",
    "فرانسيس بيكون" to "Francis Bacon",
)

fun getTodayQuote(): Quote {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val quote = ARABIC_QUOTES[(day - 1) % ARABIC_QUOTES.size]
    if (com.example.i18n.AppLanguage.isArabic) return quote
    // English: show the newest downloaded quote when there is one (it keeps changing).
    QuoteSync.latest()?.let { return it }
    return Quote(
        text = QUOTES_EN[quote.text] ?: quote.text,
        author = AUTHORS_EN[quote.author] ?: quote.author
    )
}
