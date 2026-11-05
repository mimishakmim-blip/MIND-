package com.example.data.constants

data class FoodItem(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

val COMMON_FOODS = listOf(
    FoodItem("أرز (كوب)", 205, 4f, 45f, 0.5f),
    FoodItem("خبز (رغيف)", 80, 3f, 15f, 1f),
    FoodItem("دجاج مشوي (100غ)", 165, 31f, 0f, 3.6f),
    FoodItem("لحم بقري (100غ)", 250, 26f, 0f, 15f),
    FoodItem("سمك (100غ)", 140, 22f, 0f, 5f),
    FoodItem("بيضة مسلوقة", 78, 6f, 0.6f, 5f),
    FoodItem("حليب (كوب)", 150, 8f, 12f, 8f),
    FoodItem("جبن (30غ)", 100, 6f, 1f, 8f),
    FoodItem("زبادي (علبة)", 100, 5f, 10f, 4f),
    FoodItem("تفاح (حبة)", 95, 0.5f, 25f, 0.3f),
    FoodItem("موز (حبة)", 105, 1.3f, 27f, 0.4f),
    FoodItem("برتقال (حبة)", 62, 1.2f, 15f, 0.2f),
    FoodItem("عنب (كوب)", 104, 1f, 27f, 0.2f),
    FoodItem("بطيخ (كوب)", 46, 0.9f, 12f, 0.2f),
    FoodItem("تمر (3 حبات)", 60, 0.5f, 16f, 0f),
    FoodItem("عدس (كوب)", 230, 18f, 40f, 0.8f),
    FoodItem("حمص (كوب)", 270, 15f, 45f, 4f),
    FoodItem("فول (كوب)", 220, 15f, 40f, 1f),
    FoodItem("فاصوليا (كوب)", 225, 15f, 40f, 1f),
    FoodItem("سلطة خضراء", 50, 2f, 8f, 1f),
    FoodItem("طماطم (حبة)", 22, 1f, 5f, 0.2f),
    FoodItem("خيار (حبة)", 15, 0.6f, 3.5f, 0.1f),
    FoodItem("جزر (حبة)", 25, 0.5f, 6f, 0.1f),
    FoodItem("بطاطس مسلوقة (100غ)", 87, 2f, 20f, 0.1f),
    FoodItem("بصل (حبة)", 40, 1f, 9f, 0.1f),
    FoodItem("ثوم (فص)", 4, 0.2f, 1f, 0f),
    FoodItem("زيت زيتون (ملعقة)", 119, 0f, 0f, 13.5f),
    FoodItem("زبدة (ملعقة)", 102, 0.1f, 0f, 11.5f),
    FoodItem("عسل (ملعقة)", 64, 0.06f, 17f, 0f),
    FoodItem("شوكولاتة (30غ)", 160, 2f, 17f, 9f)
)

private val FOOD_NAMES_EN: Map<String, String> = mapOf(
    "أرز (كوب)" to "Rice (cup)",
    "خبز (رغيف)" to "Bread (loaf)",
    "دجاج مشوي (100غ)" to "Grilled chicken (100 g)",
    "لحم بقري (100غ)" to "Beef (100 g)",
    "سمك (100غ)" to "Fish (100 g)",
    "بيضة مسلوقة" to "Boiled egg",
    "حليب (كوب)" to "Milk (cup)",
    "جبن (30غ)" to "Cheese (30 g)",
    "زبادي (علبة)" to "Yogurt (tub)",
    "تفاح (حبة)" to "Apple (1)",
    "موز (حبة)" to "Banana (1)",
    "برتقال (حبة)" to "Orange (1)",
    "عنب (كوب)" to "Grapes (cup)",
    "بطيخ (كوب)" to "Watermelon (cup)",
    "تمر (3 حبات)" to "Dates (3)",
    "عدس (كوب)" to "Lentils (cup)",
    "حمص (كوب)" to "Chickpeas (cup)",
    "فول (كوب)" to "Fava beans (cup)",
    "فاصوليا (كوب)" to "Beans (cup)",
    "سلطة خضراء" to "Green salad",
    "طماطم (حبة)" to "Tomato (1)",
    "خيار (حبة)" to "Cucumber (1)",
    "جزر (حبة)" to "Carrot (1)",
    "بطاطس مسلوقة (100غ)" to "Boiled potatoes (100 g)",
    "بصل (حبة)" to "Onion (1)",
    "ثوم (فص)" to "Garlic (clove)",
    "زيت زيتون (ملعقة)" to "Olive oil (spoon)",
    "زبدة (ملعقة)" to "Butter (spoon)",
    "عسل (ملعقة)" to "Honey (spoon)",
    "شوكولاتة (30غ)" to "Chocolate (30 g)",
)

/** Food name in the current app language. */
fun foodDisplayName(arabicName: String): String =
    if (com.example.i18n.AppLanguage.isArabic) arabicName else FOOD_NAMES_EN[arabicName] ?: arabicName
