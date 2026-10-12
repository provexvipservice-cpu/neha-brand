package com.example.ui.util

object AppStrings {
    fun brandName(isSinhala: Boolean) = if (isSinhala) "නේහා බ්‍රෑන්ඩ්" else "NEHA BRAND"
    fun owner(isSinhala: Boolean) = if (isSinhala) "හිමිකරු: සත්සර ඩී" else "Owner: Sathsara D"
    fun tagline(isSinhala: Boolean) = if (isSinhala) "ඔබේ වෙළඳසැල දක්ෂ ලෙස දියුණු කරගන්න" else "Grow Your Shop Smart"
    fun footer(isSinhala: Boolean) = if (isSinhala)
        "© නේහා බ්‍රෑන්ඩ් - හිමිකරු සත්සර ඩී - ඔබේ වෙළඳසැල දක්ෂ ලෙස දියුණු කරගන්න"
    else
        "© NEHA BRAND - Owner Sathsara D - Grow Your Shop Smart"

    fun loginTitle(isSinhala: Boolean) = if (isSinhala) "පිවිසුම" else "Sign In"
    fun emailOtp(isSinhala: Boolean) = if (isSinhala) "ඊමේල් OTP කේතය" else "Email OTP Code"
    fun affiliateCrew(isSinhala: Boolean) = if (isSinhala) "ඇෆිලියේට් කණ්ඩායම" else "AFFILIATE CREW"
    fun brandCrew(isSinhala: Boolean) = if (isSinhala) "සේවා කණ්ඩායම (BRAND CREW)" else "BRAND CREW"
    fun huntShops(isSinhala: Boolean) = if (isSinhala) "වෙළඳසැල් සොයාගන්න (5-7% කොමිස්)" else "Hunt Shops & Earn 5-7%"
    fun serviceShops(isSinhala: Boolean) = if (isSinhala) "සේවා කාර්යයන් (ප්‍රවර්ධන, බිල්පත්, තොග)" else "Service Shops (Promotion, Ebill, Stock)"
    fun availableBalance(isSinhala: Boolean) = if (isSinhala) "ලබාගත හැකි ශේෂය" else "Available Balance"
    fun pendingBalance(isSinhala: Boolean) = if (isSinhala) "පොරොත්තු ශේෂය" else "Pending Balance"
    fun totalEarned(isSinhala: Boolean) = if (isSinhala) "සමස්ත ඉපැයීම්" else "Total Earned"
    fun bankDetails(isSinhala: Boolean) = if (isSinhala) "බැංකු තොරතුරු" else "Bank Details"
    fun liveCamera(isSinhala: Boolean) = if (isSinhala) "සජීවී ඡායාරූපය ගන්න" else "Take Live Photo"
    fun trainingAcademy(isSinhala: Boolean) = if (isSinhala) "පුහුණු ඇකඩමිය" else "Training Academy"
    fun adminDashboard(isSinhala: Boolean) = if (isSinhala) "පරිපාලක පුවරුව" else "Admin Dashboard"
}
