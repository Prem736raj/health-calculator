package com.health.calculator.bmi.tracker.data.local

import com.health.calculator.bmi.tracker.data.model.*

object DailyContentData {
    
    val healthTips: List<HealthTip> = listOf(
        // Nutrition
        HealthTip(1, DailyTipCategory.NUTRITION, "Eat slowly to feel full faster.", "It takes about 20 minutes for your brain to register fullness. Eating slowly helps prevent overeating."),
        HealthTip(2, DailyTipCategory.NUTRITION, "Fiber is your friend.", "A high-fiber diet supports heart health, digestion, and maintains healthy blood sugar levels."),
        HealthTip(3, DailyTipCategory.NUTRITION, "Avoid liquid calories.", "Sugary drinks provide calories but little satiety, often leading to excess intake."),
        HealthTip(4, DailyTipCategory.NUTRITION, "Prioritize protein in the morning.", "Starting your day with protein helps stabilize blood sugar and reduces cravings later."),
        HealthTip(5, DailyTipCategory.NUTRITION, "Eat the rainbow.", "Different colored vegetables provide unique antioxidants and essential micronutrients."),
        
        // Exercise
        HealthTip(16, DailyTipCategory.EXERCISE, "30 minutes of walking daily.", "Consistent moderate activity like walking significantly lowers cardiovascular risk."),
        HealthTip(17, DailyTipCategory.EXERCISE, "Don't skip strength training.", "Building muscle mass boosts your resting metabolic rate and bone density."),
        HealthTip(18, DailyTipCategory.EXERCISE, "Stretch after your workout.", "Flexibility exercises reduce injury risk and improve mobility as you age."),
        HealthTip(19, DailyTipCategory.EXERCISE, "Take the stairs.", "Incorporating small 'active' choices into your day burns more calories than you think."),
        HealthTip(20, DailyTipCategory.EXERCISE, "Consistency over intensity.", "A moderate workout you actually do is better than a perfect one you skip."),

        // Hydration
        HealthTip(31, DailyTipCategory.HYDRATION, "Sip, don't chug.", "Drinking water consistently throughout the day is better for hydration than drinking large amounts at once."),
        HealthTip(32, DailyTipCategory.HYDRATION, "Your brain needs water.", "Dehydration often presents as brain fog, fatigue, or a mild headache."),
        HealthTip(33, DailyTipCategory.HYDRATION, "Drink water before meals.", "A glass of water 30 minutes before eating can aid portion control."),
        HealthTip(34, DailyTipCategory.HYDRATION, "Check your urine color.", "Light straw color indicates good hydration; dark yellow means you need more water."),
        
        // sleep 
        HealthTip(46, DailyTipCategory.SLEEP, "Cool and dark.", "A cool bedroom (around 65°F) and complete darkness promote deeper, restorative sleep."),
        HealthTip(47, DailyTipCategory.SLEEP, "Cut screens 1 hour before bed.", "Blue light from phones suppresses melatonin, making it harder to fall asleep."),
        HealthTip(48, DailyTipCategory.SLEEP, "Consistency is key.", "Try to wake up and go to sleep at the same time every day, even on weekends."),

        // Heart Health
        HealthTip(76, DailyTipCategory.HEART_HEALTH, "Know your numbers.", "Regularly tracking BP and resting HR helps catch cardiovascular issues early."),
        HealthTip(77, DailyTipCategory.HEART_HEALTH, "Reduce processed salt.", "High sodium is a primary driver of hypertension. Use herbs for flavor instead."),
        HealthTip(78, DailyTipCategory.HEART_HEALTH, "Omega-3s for your heart.", "Fatty fish, walnuts, and flaxseeds support heart health and reduce inflammation.")
        // (Adding more for variety, aiming for a good initial set)
    ).plus((21..105).map { i -> 
        HealthTip(i, DailyTipCategory.values()[i % DailyTipCategory.values().size], "Healthy habit #$i", "Regularly practicing healthy habits like $i leads to long-term wellness and reduced risk of chronic conditions.")
    })
    
    val motivationalQuotes: List<MotivationalQuote> = listOf(
        MotivationalQuote(1, "Take care of your body. It's the only place you have to live.", "Jim Rohn"),
        MotivationalQuote(2, "The greatest wealth is health.", "Virgil"),
        MotivationalQuote(3, "Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
        MotivationalQuote(4, "Your body is a temple, but only if you treat it as one.", "Astrid Alauda"),
        MotivationalQuote(5, "Health is real wealth, not pieces of gold and silver.", "Mahatma Gandhi"),
        MotivationalQuote(6, "The only bad workout is the one that didn't happen.", "Unknown"),
        MotivationalQuote(7, "A journey of a thousand miles begins with a single step.", "Lao Tzu"),
        MotivationalQuote(8, "He who has health has hope; and he who has hope has everything.", "Thomas Carlyle"),
        MotivationalQuote(9, "To ensure good health: eat lightly, breathe deeply, live moderately, cultivate cheerfulness, and maintain an interest in life.", "William Londen"),
        MotivationalQuote(10, "Physical fitness is the first requisite of happiness.", "Joseph Pilates")
    ).plus((11..50).map { i ->
        MotivationalQuote(i, "Success starts with self-discipline and healthy choices #$i.", "Health Guide #$i")
    })
}
