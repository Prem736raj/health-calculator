// File: com/health/calculator/bmi/tracker/data/model/BMRAgeCurveData.kt
package com.health.calculator.bmi.tracker.data.model

object BMRAgeCurveData {

    // Average BMR values by age group (kcal/day)
    // Based on population averages from medical literature
    data class AgeBMRPoint(
        val age: Int,
        val maleBMR: Float,
        val femaleBMR: Float
    )

    val ageCurvePoints = listOf(
        AgeBMRPoint(15, 1820f, 1460f),
        AgeBMRPoint(20, 1850f, 1450f),
        AgeBMRPoint(25, 1830f, 1430f),
        AgeBMRPoint(30, 1800f, 1400f),
        AgeBMRPoint(35, 1770f, 1380f),
        AgeBMRPoint(40, 1740f, 1350f),
        AgeBMRPoint(45, 1710f, 1325f),
        AgeBMRPoint(50, 1675f, 1300f),
        AgeBMRPoint(55, 1640f, 1275f),
        AgeBMRPoint(60, 1600f, 1250f),
        AgeBMRPoint(65, 1565f, 1225f),
        AgeBMRPoint(70, 1530f, 1200f),
        AgeBMRPoint(75, 1490f, 1175f),
        AgeBMRPoint(80, 1450f, 1150f)
    )

    fun getAverageBMRForAge(age: Int, isMale: Boolean): Float {
        val clampedAge = age.coerceIn(15, 80)
        val points = ageCurvePoints

        // Find surrounding points for interpolation
        val lower = points.lastOrNull { it.age <= clampedAge } ?: points.first()
        val upper = points.firstOrNull { it.age >= clampedAge } ?: points.last()

        if (lower.age == upper.age) {
            return if (isMale) lower.maleBMR else lower.femaleBMR
        }

        val fraction = (clampedAge - lower.age).toFloat() / (upper.age - lower.age).toFloat()
        val lowerBMR = if (isMale) lower.maleBMR else lower.femaleBMR
        val upperBMR = if (isMale) upper.maleBMR else upper.femaleBMR

        return lowerBMR + (upperBMR - lowerBMR) * fraction
    }

    fun getComparisonText(userBMR: Float, age: Int, isMale: Boolean): String {
        val average = getAverageBMRForAge(age, isMale)
        val diff = userBMR - average
        val percentage = (diff / average * 100f)
        val genderText = if (isMale) "men" else "women"

        return when {
            percentage > 15f -> "Your BMR is significantly higher than average for $genderText your age " +
                    "(+${percentage.toInt()}%). This is often seen with higher muscle mass or larger body frame."
            percentage > 5f -> "Your BMR is above average for $genderText your age " +
                    "(+${percentage.toInt()}%). This could indicate good muscle mass."
            percentage > -5f -> "Your BMR is right around the average for $genderText your age. " +
                    "This is typical and healthy."
            percentage > -15f -> "Your BMR is slightly below average for $genderText your age " +
                    "(${percentage.toInt()}%). This is normal and can vary with body composition."
            else -> "Your BMR is notably below average for $genderText your age " +
                    "(${percentage.toInt()}%). Consider discussing metabolism with your healthcare provider."
        }
    }

    fun getDecadeDeclineText(age: Int): String {
        return when {
            age < 25 -> "Your metabolism is near its peak! It will gradually decrease about 1-2% per decade starting around age 20."
            age < 35 -> "You're in the early stages of natural metabolic decline. Stay active to minimize the effect."
            age < 45 -> "By this age, your BMR has likely decreased about 3-5% from your peak. Regular exercise helps maintain it."
            age < 55 -> "Your metabolism has naturally slowed. Strength training is especially important to preserve muscle mass and BMR."
            age < 65 -> "Metabolic decline accelerates in this decade. Focus on protein intake and resistance exercise."
            else -> "Natural metabolic slowing is significant at this age. Staying active and eating protein-rich foods helps maintain metabolic health."
        }
    }
}
