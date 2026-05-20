package ca.arzook.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ContentScreen(title: String, onBack: () -> Unit) {
    var isFarsi by remember { mutableStateOf(false) }
    var fullscreenVideoId by rememberSaveable { mutableStateOf<String?>(null) }

    fullscreenVideoId?.let { id ->
        Dialog(
            onDismissRequest = { fullscreenVideoId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            LandscapeEffect()
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                YoutubePlayer(videoId = id, modifier = Modifier.fillMaxSize())
                TextButton(
                    onClick = { fullscreenVideoId = null },
                    modifier = Modifier.align(Alignment.TopStart)
                ) { Text("✕ Close", color = Color.White) }
            }
        }
    }

    val direction = if (isFarsi) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextButton(onClick = { isFarsi = !isFarsi }) {
                Text(if (isFarsi) "EN" else "FA", fontWeight = FontWeight.Bold)
            }
            when (title) {
                "About Us" -> if (isFarsi) AboutUsFa() else AboutUsContent()
                "How It Works" -> if (isFarsi) HowItWorksFa() else HowItWorksContent()
                "FAQ" -> if (isFarsi) FaqFa(onFullscreen = { fullscreenVideoId = it }) else FaqContent(onFullscreen = { fullscreenVideoId = it })
                "Privacy Policy" -> if (isFarsi) PrivacyPolicyFa() else PrivacyPolicyContent()
                "Terms and Conditions" -> if (isFarsi) TermsFa() else TermsContent()
                "Contact Us" -> if (isFarsi) ContactUsFa() else ContactUsContent()
            }
        }
    }
}

private fun toFarsiTitle(title: String) = when (title) {
    "About Us" -> "درباره ما"
    "How It Works" -> "نحوه کار"
    "FAQ" -> "سوالات متداول"
    "Privacy Policy" -> "سیاست حریم خصوصی"
    "Terms and Conditions" -> "شرایط و ضوابط"
    else -> title
}

@Composable private fun SectionTitle(text: String) =
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))

@Composable private fun Body(text: String) =
    Text(text, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(bottom = 12.dp))

@Composable fun AboutUsContent() {
    Body("Arzook is a Canadian online currency exchange platform that offers convenient, transparent services for exchanging and transferring money overseas. Customers can choose their desired exchange rates and use the platform to exchange and send money from anywhere, anytime.")
    Body("As a FINTRAC registered MSB, Arzook is subject to certain regulations set by the Financial Transactions and Reports Analysis Centre of Canada. Arzook is also available 24/7 to serve customers worldwide. With its online platform, customers can access Arzook at any time, making it a convenient choice for all of your currency exchange and money transfer needs. Plus, with a secure, trusted payment process in place, you can trust that your transactions with Arzook are safe and secure. Whether you're looking to exchange or transfer money, Arzook has you covered.")
}

@Composable fun HowItWorksContent() {
    ScenarioCard(
        title = "Scenario #1: Seller initiates the transaction",
        note = "Note: Seller is the person who is sending money to Iran",
        note2 = "Note: Cheque can be sent for higher amounts that cannot be e-Transferred. Please contact us when needed.",
        rows = listOf(
            StepRow(seller = "Seller", buyer = "Buyer", isHeader = true),
            StepRow(seller = "1. Seller creates a Selling", icon = StepIcon.Dot, iconOnSeller = true),
            StepRow(seller = "2. Seller sends an e-Transfer", icon = StepIcon.Arrow, iconOnSeller = true),
            StepRow(buyer = "3. Buyer locks the Selling", icon = StepIcon.Arrow, iconOnSeller = false),
            StepRow(buyer = "4. Buyer deposits Rial amount", icon = StepIcon.Arrow, iconOnSeller = false),
            StepRow(buyer = "5. Buyer receives and deposits the e-Transfer", icon = StepIcon.Arrow, iconOnSeller = false),
            StepRow(seller = "6. Seller receives Rial amount", icon = StepIcon.Dot, iconOnSeller = true),
        )
    )
    Spacer(Modifier.height(20.dp))
    ScenarioCard(
        title = "Scenario #2: Buyer initiates the transaction",
        note = "Note: Buyer is the person who is sending money to Canada",
        rows = listOf(
            StepRow(seller = "Seller", buyer = "Buyer", isHeader = true),
            StepRow(buyer = "1. Buyer creates a Buying", icon = StepIcon.Dot, iconOnSeller = false),
            StepRow(buyer = "2. Buyer deposits Rial", icon = StepIcon.Arrow, iconOnSeller = false),
            StepRow(seller = "3. Seller locks the Buying", icon = StepIcon.Arrow, iconOnSeller = true),
            StepRow(seller = "4. Seller sends an e-Transfer", icon = StepIcon.Arrow, iconOnSeller = true),
            StepRow(buyer = "5. Buyer receives and deposits the e-Transfer", icon = StepIcon.Arrow, iconOnSeller = false),
            StepRow(seller = "6. Seller receives Rial amount", icon = StepIcon.Dot, iconOnSeller = true),
        )
    )
}



private enum class StepIcon { Arrow, Dot }
private data class StepRow(
    val seller: String = "",
    val buyer: String = "",
    val icon: StepIcon? = null,
    val iconOnSeller: Boolean = true,
    val isHeader: Boolean = false
)

@Composable
private fun ScenarioCard(title: String, rows: List<StepRow>, note: String, note2: String = "") {
    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier.padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(BorderStroke(1.dp, Color.Red), shape = RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
        rows.forEach { row ->
            StepRowItem(row)
        }
        Spacer(Modifier.height(8.dp))
        Text(note, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 14.sp)
        if (note2.isNotEmpty()) {
            Text(note2, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StepRowItem(row: StepRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.seller,
            color = SellerColor,
            fontWeight = if (row.isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.42f)
        )
        Box(modifier = Modifier.weight(0.16f), contentAlignment = Alignment.Center) {
            row.icon?.let {
                val color = if (row.iconOnSeller) SellerColor else BuyerColor
                Text(
                    text = if (it == StepIcon.Arrow) "▼" else "●",
                    color = color,
                    fontSize = if (it == StepIcon.Arrow) 22.sp else 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = row.buyer,
            color = BuyerColor,
            fontWeight = if (row.isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Start,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.42f)
        )
    }
}

@Composable fun FaqContent(onFullscreen: (String) -> Unit = {}) {
    FaqItem(
        question = "What are the benefits of using Arzook for money transfers?",
        answer = "Arzook offers several benefits to make your money transfer experience faster, simpler, and more efficient. " +
                "Our platform utilizes intelligent design and cutting-edge technologies to streamline the process, and allows users to set their own exchange rates. " +
                "With Arzook, you can quickly exchange currency at a competitive rate, 24 hours a day, 7 days a week, all online.\n" +
                "Additionally, we also offer:\n" +
                "  - High-level security measures to protect your personal and financial information\n" +
                "  - Convenience of being able to make transfers from anywhere with an Internet connection\n" +
                "  - Reliability and transparency in our transactions\n" +
                "  - Competitive fees\n" +
                "  - Transparent and consistent exchange rates\n" +
                "  - Excellent customer service and support"
    )
    FaqItem(
        question = "What should I do if I have a problem that is not listed in the FAQ?",
        answer = "If you have a problem that is not addressed in our FAQs, please contact Arzook customer support. Our team will be happy to assist you. " +
                "When you contact us, please provide as much information as possible about your problem or question, including any error messages, screenshots, or details of the specific transaction. " +
                "This will help us to quickly and accurately assess the issue and provide you with an appropriate solution."
    )
    FaqItem(
        question = "Where can I view my previous transactions on Arzook?",
        answer = "On Arzook, you can view a complete record of all your previous transactions by navigating to the Completed Buying and Completed Selling pages within your account. " +
                "Here, you will find detailed information on each transaction, including the date and amount. " +
                "Additionally, you have the option to print or download a receipt for each transaction in PDF format for your records."
    )
    FaqItem(
        question = "Can I use my Gmail to create an Arzook account?",
        answer = "Yes, you have the option to create an Arzook account using your Gmail account. To do this, simply click the Sign Up with Google button on the registration page. " +
                "Alternatively, you can create an Arzook account by providing a valid email address and password of your choice on the registration page."
    )
    FaqItem(
        question = "Why I haven't received any email from Arzook after signing up in the website?",
        answer = "It's possible that the email was filtered into your spam or junk folder. We recommend checking these folders first. " +
                "If you're still unable to locate the email, please contact our customer support team with the email address you used to sign up, so that we can assist you in resolving the issue.\n" +
                "To ensure that future emails from Arzook are not filtered into spam, we suggest:\n" +
                " - Adding our email address to your contact list\n" +
                " - Marking emails from Arzook as Not Spam if they have been incorrectly filtered into your spam folder"
    )
    FaqItem(
        question = "How to send money from Canada to Iran creating a new Selling?",
        answer = " - Head to My Selling and create a new Selling.\n" +
                " - Choose the payee that you want to send exchanged money to. You can set this later if you do not have Sheba yet.\n" +
                " - Send an e-Transfer to the provided email address, e.g. deposit@arzook.ca.\n" +
                " - Once a buyer locks your Selling and successfully deposits Rial to Arzook, your e-Transfer will be forwarded to the buyer.\n" +
                " - As soon as the Buyer deposits your e-Transfer, your payee will receive the exchanged Rial in Iran.\n\n" +
                "Note 1: Follow the instructions in the Selling for e-Transfer message and security answer. This will be used to process your e-Transfer.\n" +
                "Note 2: Arzook is a highly competitive market and finding the right exchange rate is important. Check the HOME page for the current best rates prior to deciding on your desired exchange rate.",
        videoId = "7_J0CRi731I",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "How to send money from Canada to Iran taking an existing Buying?",
        answer = " - Find your desired rate and amount from the list of top Buyings on the HOME page.\n" +
                " - Clicking on the Sell button, you will be asked to review the Buying. Proceed to lock the Buying if interested. This will create a Selling record and send you to My Selling.\n" +
                " - Choose the payee that you want to send the exchanged money to. You can set this later if you do not have Sheba yet.\n" +
                " - Send an e-Transfer to the provided email address, e.g. deposit@arzook.ca.\n" +
                " - The e-Transfer will be sent to the Buyer. As soon as the Buyer deposits the e-Transfer, you will receive the exchanged Rial in Iran.\n\n" +
                "Note 1: Follow the instructions in the Selling for e-Transfer message and security answer.\n" +
                "Note 2: The e-Transfer must be received by Arzook platform before the lock expiry time. Otherwise, the system will automatically delete your Selling.",
        videoId = "sXJ3UudTHcA",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "How do the exchange rates get set on Arzook?",
        answer = "On Arzook, either buyers or sellers can initiate trades by creating a buying or selling offer with their desired exchange rate. " +
                "Buyers can create a buying offer with their desired exchange rate and wait for a seller to accept and lock the buying at that rate. " +
                "On the other hand, sellers can also create a selling offer with their desired exchange rate and wait for a buyer to accept and lock the selling at that rate. " +
                "The exchange rate is fixed once the buying or selling is locked by the counterparty. " +
                "As a result, exchange rates are determined by the buying and selling activity on the Arzook platform."
    )
    FaqItem(
        question = "How can I find out the right exchange rate?",
        answer = "1) Check the list of the current Arzook Sellings (Best Rates posted).\n" +
                "2) Check traditional currency exchange providers for their exchange rates. You should be able to exchange your currency with a better rate on Arzook. Think competitively."
    )
    FaqItem(
        question = "Why doesn't Arzook accept credit cards?",
        answer = "Arzook currently does not accept credit card as a form of payment because it can hinder the rapid currency exchange and fund transfer that our platform aims to provide. " +
                "The process of credit card transactions requires complex authentication and clearance procedures, which may take several days to complete. " +
                "Additionally, credit card transactions also come with a higher fee structure as compared to other forms of payment. " +
                "To ensure that Arzook provides a fast and cost-effective service, we have chosen to support other forms of payment such as online bank transfer."
    )
    FaqItem(
        question = "I have created a Selling, can I change the exchange rate?",
        answer = "Yes, you can change the exchange rate of a Selling offer as long as it has not been locked by a potential buyer. " +
                "Once a potential buyer locks your selling, you won't be able to change the exchange rate. " +
                "You will have to wait for the buyer to cancel the lock to make changes on the rate."
    )
    FaqItem(
        question = "Can I cancel an e-Transfer that I have sent to Arzook?",
        answer = "Yes, you can cancel an e-transfer you have sent to Arzook, but prior to doing so, please check the corresponding selling to ensure that it has not been locked by a buyer. " +
                "If the Selling is not locked, you can simply cancel the e-transfer in your bank account, and Arzook will receive a cancellation notice and automatically process the cancellation. " +
                "However, if the selling is locked, please do not cancel the e-transfer as this will result in a disappointment for the buyer who has locked the selling."
    )
    FaqItem(
        question = "How to send money from Iran to Canada creating a new Buying?",
        answer = " - Head to My Buying and create a new Buying.\n" +
                " - Deposit the required amount of Rial into Arzook account, and then notify us by clicking on the Deposit button in the Buying.\n" +
                " - Once your payment is confirmed, the Buying will be available in the top Buyings list.\n" +
                " - As soon as a seller locks your Buying and sends e-Transfer to Arzook, the e-Transfer will be forwarded to you.\n" +
                " - We will deposit Rial into the Seller after you deposit the e-Transfer.\n\n" +
                "Note 1: Follow the instructions in the Buying to deposit Rial.\n" +
                "Note 2: Arzook is a highly competitive market. Check the HOME page for the current best rates prior to deciding on your desired exchange rate.\n" +
                "Note 3: The e-Transfer password (security answer) will be displayed on your Buying once locked by a Seller.",
        videoId = "sfkoBcsh9As",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "How to send money from Iran to Canada taking an existing Selling?",
        answer = " - Find your desired rate and amount from the list of top Sellings on the HOME page.\n" +
                " - Clicking on the Buy button, you will be asked to review the Selling. Proceed to lock the Selling if interested. This will create a Buying record and send you to My Buying.\n" +
                " - Deposit the required amount of Rial into Arzook account, and then notify us by clicking on the Deposit button in Buying.\n" +
                " - Once your payment is confirmed, we will forward you the e-Transfer.\n\n" +
                "Note 1: You must notify Arzook of the Rial deposit from the Buying before the lock expiry time. Otherwise, the system will automatically delete your Buying.\n" +
                "Note 2: The e-Transfer password, security answer, will be displayed on your Buying once deposited.",
        videoId = "TsALuxwm3rc",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "Why does Arzook give a limited time for buyers to review and lock a Selling when creating a new Buying?",
        answer = "Arzook is a highly competitive platform, and many users may be actively looking to buy a Selling. " +
                "To maintain the dynamic nature of the market, buyers need to quickly decide when it comes to locking a Selling. " +
                "Therefore, when a buyer creates a new Buying, Arzook allows them a limited time to review the available sellings and lock the one that they want to buy. " +
                "This ensures that the platform remains active, and transactions are processed quickly."
    )
    FaqItem(
        question = "I have deposited a Buying. Can I cancel the Buying and ask for refund?",
        answer = "If you have created a Buying and it has not yet been locked by a seller, you can cancel the buying and ask for a refund of your deposit. " +
                "To do this, you will need to contact Arzook customer support, who will process the cancellation and refund. " +
                "However, once a seller has locked the buying, the trade is considered final and deposits are not refundable."
    )
}

@Composable fun PrivacyPolicyContent() {
    SectionTitle("What we collect and why")
    Body("We collect your name, contact information, data and time of your account creation, IP address and device identifier. We also collect your hashed password if you create an account using the signup page. If you use Social Login to create account, we will get your full name, profile picture and contact info from the Social Sign On provider (Google).\nWe also collect your banking information and your payees' banking information if they are different from yours.")
    SectionTitle("Collection and Use of Personal Information")
    Body("Arzook collects your personal information to be able to serve you. We need your email address to communicate with you and also send you e-Transfers. We also collect & validate your valid photo id to know you. This is a FINTRAC mandate.")
    SectionTitle("Disclosure of Personal Information")
    Body("We may share your personal information with other parties when needed to conduct the business.")
    SectionTitle("Data Protection Policies")
    Body("In order to secure your account, your password gets hashed, and the hashed password will be used for authentication. The hashed password is irreversible so nobody can find out what you have chosen as your password.\nWe also encrypt our databases to protect the entire data we store. All user communications with Arzook is via HTTPS protocol which encrypts data in transit. Our transactional email to customers are also TLS encrypted.")
    SectionTitle("Updates to the Privacy Policy")
    Body("This Privacy Policy is current as of the \"Last Updated\" date which appears at the bottom of the page. Your continued use of the Arzook Platform after any change of our Privacy Policy will constitute your acceptance of the revised terms of this Privacy Policy. We may update this Privacy Policy from time to time as required to reflect changes to our privacy practices. We encourage you to periodically review this page for the latest information on our privacy practices.\nWe love to hear from you. If you would like to access your information, or would like to receive a copy of our this Privacy Policy, or have any questions and concerns with regards to the Privacy Policy, please contact us.")
    Body("\nLast Updated: December 23, 2020")
}

@Composable fun TermsContent() {
    Body("Please read these Terms carefully. Access to, and use of Arzook services (\"Services\"), and the Arzook website https://www.arzook.ca/ (\"Website\"), including any of its content, is conditional on your agreement to these Terms. By creating an account, or by using or visiting our Website, you are bound to these Terms.")
    FaqItem("1. Responsibility of Accounts",
        "If you create an account on the Website, you are responsible for maintaining the security of your account, and you are also held responsible for all activities that occur under the account and any other actions taken in connection with the account. You agree to provide and maintain accurate, current and complete information, including your contact information for notices and other communications from us. You may not use false or misleading information in connection to your account.\nYou are responsible for taking reasonable steps to maintain the confidentiality of your username and password. You must immediately notify Arzook of any unauthorized uses of your information, your account or any other security breaches.")
    FaqItem("2. Responsibility of Users of the Website and/or Services",
        "Your access to, and all of your use of the Website and/or Services must be lawful and must be in compliance with these Terms, and all Anti Money Laundering (AML) policies of your country of residence.\nWhen accessing or using the Website and/or Services, you must behave in a civil and respectful manner at all times. We specifically prohibit any use of the Website and/or Services for any kind of money laundering activities.\nYou are responsible for taking precautions as necessary to protect yourself and your computer systems from viruses, worms, Trojan horses, and other harmful or destructive content.")
    FaqItem("3. Fees and Payments",
        "By selling or buying a currency and transferring overseas, you agree to pay the Service Charge which will be determined at the time of adding a new selling or creating a new buying (locking an existing selling). The Service Charge will be added to the total transaction amount. You agree to pay the Service Charge along with the total transaction amount, otherwise your selling or buying transaction will not be processed.\nConfigurations and prices of the Services are subject to change at any time, at Arzook's discretion.")
    FaqItem("4. Intellectual Property Rights",
        "Arzook, the Arzook logo, and all other trademarks, service marks, graphics used in connection with the Website and Services, are trademarks or registered trademarks of Arzook. Your use of the Website grants you no right or license to reproduce or otherwise use any Arzook trademarks.")
    FaqItem("5. Termination",
        "Arzook may terminate any agreement and access to your account, if the Services or any part thereof, are no longer legally available in your jurisdiction, or are no longer commercially viable, at Arzook's discretion.")
    FaqItem("6. Changes",
        "The configurations and specifications of the Website and the Services may be amended and/or updated from time to time, at the sole discretion of Arzook. You are bound by any such changes or updates.")
    FaqItem("7. Limitation of Warranties of Arzook",
        "Arzook makes no warranties or representations whatsoever with respect to your access to or use of the Website and/or Services, or any linked site will be uninterrupted or free of errors or omissions, that defects will be corrected, or that the Website and/or Services, or any linked site is free of computer viruses or other harmful components. If you are dissatisfied with the Website, your sole remedy is to discontinue using the Website.")
    FaqItem("8. Limitation of Liability of Arzook",
        "Under no circumstances shall any party, its subsidiaries and affiliates, their respective directors, officers, employees or agents, and other representatives, be liable for any indirect, consequential, incidental, special, or punitive damages, including but not limited to lost profits and business interruption, whether in contract or in tort, including negligence, arising in any way from the use of the Website and/or Services.")
    FaqItem("9. Your Representations and Warranties",
        "You represent and warrant that your use of the Website and/or Services will be in accordance with any agreement between you and Arzook, the Arzook Privacy Policy, these Terms, and with any applicable laws and regulations, including without limitation any local laws or regulations in your country, state, city, or other governmental area, regarding online conduct, and including all applicable laws regarding currency exchange and money transfer from the country in which you reside.")
    FaqItem("10. Indemnification",
        "Subject to the limitations set forth herein, the Parties agree to defend, indemnify, and hold each other harmless, including its subsidiaries and affiliates, their respective directors, officers, employees or agents, and other representatives, from and against all claims, losses, damages, liabilities, and costs, arising out of, relating to or in connection with a violation of these Terms.\nYou understand and agree that, by using the Services, you are solely responsible for any data, including personally identifiable information, collected or processed via our Services.")
    FaqItem("11. Miscellaneous",
        "Regarding the security, confidentiality and integrity of data, each party is responsible for maintaining appropriate technical and organizational measures for the protection of data processed on their own systems and on third party systems that are in use by the involved party.\nArzook will not be liable for any delay in performing or failure to perform any of its obligations to you caused by events beyond its reasonable control.")
}

@Composable fun AboutUsFa() {
    Body("ارزوک یک پلتفرم آنلاین تبادل ارز کانادایی است که خدمات راحت و شفاف برای تبادل و انتقال پول به خارج از کشور ارائه می‌دهد. مشتریان می‌توانند نرخ ارز مورد نظر خود را انتخاب کرده و از هر جایی و در هر زمانی از پلتفرم برای تبادل و ارسال پول استفاده کنند.")
    Body("ارزوک به عنوان یک MSB ثبت‌شده در FINTRAC، تابع مقررات مرکز تحلیل تراکنش‌های مالی و گزارش‌های کانادا است. ارزوک همچنین ۲۴ ساعته و ۷ روز هفته در خدمت مشتریان سراسر جهان است. با پلتفرم آنلاین ارزوک، مشتریان می‌توانند در هر زمانی به آن دسترسی داشته باشند. با فرآیند پرداخت امن و مطمئن، می‌توانید به تراکنش‌های خود با ارزوک اطمینان داشته باشید.")
}

@Composable fun HowItWorksFa() = HowItWorksContent()

@Composable fun FaqFa(onFullscreen: (String) -> Unit = {}) {
    FaqItem(
        question = "مزایای استفاده از ارزوک چیست؟",
        answer = "ارزوک با استفاده از طراحی هوشمند خود فرآیندهای انتقال پول را بسیار ساده تر و کارآمدتر می کند.\n" +
                "نرخ ارز توسط کاربران تعیین می شود. شما می توانید به سرعت پول خود را با نرخ رقابتی 24/7 آنلاین مبادله کنید."
    )
    FaqItem(
        question = "اگر مشکلی دارم که در اینجا در قسمت پرسش‌های متداول ذکر نشده است، چه باید بکنم؟",
        answer = "لطفا با ما تماس بگیرید و تا آنجا که ممکن است اطلاعات را ارائه دهید تا بتوانیم به شما کمک کنیم."
    )
    FaqItem(
        question = "از کجا می توانم لیست تراکنش های قبلی خود را پیدا کنم؟",
        answer = "ارزوک امکان دسترسی به تراکنش های تکمیل شده را در صفحات Completed Buying و Completed Selling فراهم می کند. همچنین می توانید رسیدها را به صورت فایل PDF چاپ کنید."
    )
    FaqItem(
        question = "آیا می توانم از Gmail خود برای ایجاد یک حساب ارزوک استفاده کنم؟",
        answer = "بله، شما می توانید از اکانت جیمیل خود برای ایجاد اکانت ارزوک استفاده کنید و یا به سادگی با استفاده از ایمیل و رمز عبور، یک اکانت ارزوک بسازید."
    )
    FaqItem(
        question = "چرا پس از ساخت اکانت در وب سایت هیچ ایمیلی از ارزوک دریافت نکرده ام؟",
        answer = "ابتدا پوشه spam ایمیل خود را بررسی کنید. اگر هنوز ایمیلی از ارزوک پیدا نکردید، لطفا با ما تماس بگیرید و تا حد امکان اطلاعات خود را ارائه دهید تا بتوانیم به شما کمک کنیم."
    )
    FaqItem(
        question = "چگونه می توان از کانادا به ایران پول ارسال کرد و فروش جدیدی ایجاد کرد؟",
        answer = " - به My Selling بروید و یک فروش جدید ایجاد کنید.\n" +
                " - دریافت‌کننده‌ مورد نظر خود را انتخاب کنید. اگر هنوز شماره شبا را ندارید می‌توانید بعداً این قسمت را تکمیل کنید.\n" +
                " - ایترنسفر را به آدرس ایمیل ارائه شده، به عنوان مثال deposit@arzook.ca ارسال کنید.\n" +
                " - هنگامی که خریدار فروش شما را قفل کرد و با موفقیت معادل ریالی را به ارزوک واریز کرد، ایترنسفر شما به خریدار ارسال می شود.\n" +
                " - به محض اینکه خریدار ایترنسفر شما را نقد کرد، ریال مبادله شده در ایران به حساب دریافت کننده شما واریز می شود.\n\n" +
                "نکته 1: دستورالعمل های موجود در پیام فروش برای ایترنسفر و پاسخ امنیتی را دنبال کنید.\n" +
                "نکته 2: ارزوک بازاری بسیار رقابتی است. توصیه می کنیم قبل از تصمیم گیری در مورد نرخ ارز مورد نظر خود، صفحه اصلی را برای بهترین نرخ های کنونی بررسی کنید.",
        videoId = "7_J0CRi731I",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "چگونه با گرفتن Buying موجود از کانادا به ایران پول ارسال کنیم؟",
        answer = " - نرخ و مبلغ مورد نظر خود را از لیست خریدهای برتر در صفحه اصلی پیدا کنید.\n" +
                " - با کلیک بر روی دکمه Sell، از شما خواسته می شود تا خرید را بررسی کنید. در صورت تمایل، خرید را قفل کنید.\n" +
                " - دریافت کننده مورد نظر خود را انتخاب کنید.\n" +
                " - ایترنسفر را به آدرس ایمیل ارائه شده، به عنوان مثال deposit@arzook.ca ارسال کنید.\n" +
                " - ایترنسفر برای خریدار ارسال می شود. به محض نقد شدن ایترنسفر توسط خریدار، ریال مبادله شده در ایران به حساب فرد مورد نظر شما واریز می شود.\n\n" +
                "نکته 1: دستورالعمل های موجود در پیام فروش برای ایترنسفر و پاسخ امنیتی را دنبال کنید.\n" +
                "نکته 2: ایترنسفر باید قبل از پایان زمان قفل توسط پلتفرم ارزوک دریافت شود. در غیر این صورت، سیستم به طور خودکار Selling شما را حذف می کند.",
        videoId = "sXJ3UudTHcA",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "نرخ ارز چگونه در ارزوک تعیین می شود؟",
        answer = "ارزوک یک بازار آزاد اما رقابتی است، بنابراین مشتریان آزادانه تصمیم می گیرند و نرخ ارز مورد نظر خود را تعیین می کنند."
    )
    FaqItem(
        question = "چگونه می توانم نرخ ارز مناسب را پیدا کنم؟",
        answer = "۱) لیست کنونی فروش های ارزوک (بهترین نرخ های ارسال شده) را بررسی کنید.\n" +
                "۲) نرخ مبادله ارائه دهندگان صرافی سنتی را بررسی کنید. شما باید بتوانید ارز خود را با نرخ بهتری در ارزوک مبادله کنید. رقابتی فکر کنید."
    )
    FaqItem(
        question = "چرا ارزوک کارت اعتباری نمی پذیرد؟",
        answer = "تسویه وجه تراکنش های کارت اعتباری ممکن است چند روز طول بکشد. این امر مانع از تبادل سریع ارز و انتقال وجه در ارزوک می شود."
    )
    FaqItem(
        question = "من یک Selling ساخته ام، آیا می توانم نرخ ارز را تغییر دهم؟",
        answer = "بله، تا زمانی که فروش توسط یک خریدار بالقوه قفل نشده باشد، می توانید نرخ ارز را تغییر دهید."
    )
    FaqItem(
        question = "آیا می توانم ایترنسفری را که به ارزوک ارسال کرده ام لغو کنم؟",
        answer = "بله، حتما. اما قبل از لغو، از شما خواهش می کنیم که فروش مربوطه را بررسی کنید تا مطمئن شوید توسط هیچ خریداری قفل نشده است. " +
                "تا زمانی که فروش قفل نشده باشد، می توانید به سادگی ایترنسفر خود را در حساب بانکی خود لغو کنید. " +
                "اگر فروش قفل است، ارزوک از شما درخواست می کند که ایترنسفر را لغو نکنید زیرا این امر خریدار را ناامید می کند."
    )
    FaqItem(
        question = "چگونه از ایران به کانادا با ساخت Buying جدید، پول بفرستیم؟",
        answer = " - به My Buying بروید و خرید جدیدی ایجاد کنید.\n" +
                " - مبلغ مورد نیاز ریال را به حساب ارزوک واریز کرده و سپس با کلیک بر روی دکمه Deposit در خرید جدید به ما اطلاع دهید.\n" +
                " - پس از تایید پرداخت شما، خرید در لیست خریدهای برتر در صفحه اصلی در دسترس خواهد بود.\n" +
                " - به محض اینکه فروشنده ای خرید شما را قفل کند و ایترنسفر را به ارزوک ارسال کند، ایترنسفر برای شما فوروارد می شود.\n" +
                " - ارزوک پس از نقد ایترنسفر توسط شما، ریال را به فروشنده واریز می کند.\n\n" +
                "نکته 1: برای واریز ریالی طبق دستورالعمل Buying عمل کنید.\n" +
                "نکته 2: ارزوک بازاری بسیار رقابتی است. توصیه می کنیم قبل از تصمیم گیری در مورد نرخ ارز مورد نظر خود، صفحه اصلی را بررسی کنید.\n" +
                "نکته 3: رمز ایترنسفر (پاسخ امنیتی) پس از قفل شدن توسط فروشنده بر روی Buying شما نمایش داده می شود.",
        videoId = "sfkoBcsh9As",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "چگونه از ایران به کانادا با گرفتن Selling موجود پول بفرستیم؟",
        answer = " - نرخ و مبلغ مورد نظر خود را از لیست فروش های برتر در صفحه اصلی پیدا کنید.\n" +
                " - با کلیک بر روی دکمه خرید، از شما خواسته می شود تا فروش را بررسی کنید. در صورت تمایل، فروش را قفل کنید.\n" +
                " - مبلغ مورد نیاز ریال را به حساب ارزوک واریز کرده و سپس با کلیک بر روی دکمه Deposit در Buying جدید، به ما اطلاع دهید.\n" +
                " - پس از تایید پرداخت شما، ایترنسفر برای شما ارسال می شود.\n\n" +
                "نکته 1: شما باید قبل از انقضای زمان قفل، واریز معادل ریالی خرید را به ارزوک اطلاع دهید. در غیر این صورت، سیستم به طور خودکار خرید شما را حذف می کند.\n" +
                "نکته 2: رمز ایترنسفر، پاسخ امنیتی، پس از واریز در خرید شما نمایش داده می شود.",
        videoId = "TsALuxwm3rc",
        onFullscreen = onFullscreen
    )
    FaqItem(
        question = "چرا ارزوک هنگام ایجاد یک Buying جدید زمان کمی برای بررسی و قفل کردن یک فروش می دهد؟",
        answer = "ارزوک یک بازار بسیار رقابتی است و بسیاری از کاربران ممکن است به دنبال خرید یک Selling باشند. برای حفظ پویایی بازار، خریداران باید به سرعت در مورد قفل کردن فروش تصمیم بگیرند."
    )
    FaqItem(
        question = "اگر ریال یک Buying را واریز کرده ام. آیا می توانم آن را لغو کنم و درخواست بازپرداخت کنم؟",
        answer = "در صورتی که خرید را ایجاد کرده اید و هنوز توسط فروشنده قفل نشده است، می توانید خرید را لغو کرده و درخواست بازپرداخت کنید. " +
                "در غیر این صورت، با توجه به نهایی شدن معاملات در ارزوک، سپرده خرید قابل استرداد نمی باشد."
    )
}

@Composable
private fun FaqItem(question: String, answer: String, videoId: String = "", onFullscreen: (String) -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                HorizontalDivider()
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(12.dp)
                )
                if (videoId.isNotEmpty()) {
                    YoutubePlayer(videoId = videoId, onFullscreen = { onFullscreen(videoId) })
                }
            }
        }
    }
}

@Composable fun PrivacyPolicyFa() {
    SectionTitle("چه اطلاعاتی جمع‌آوری می‌کنیم و چرا")
    Body("ما نام، اطلاعات تماس، تاریخ و زمان ایجاد حساب، آدرس IP و شناسه دستگاه شما را جمع‌آوری می‌کنیم. همچنین رمز عبور هش‌شده شما را در صورت ثبت‌نام از طریق صفحه ثبت‌نام جمع‌آوری می‌کنیم. اگر از ورود اجتماعی (گوگل) استفاده کنید، نام کامل، تصویر پروفایل و اطلاعات تماس شما از ارائه‌دهنده دریافت می‌شود.\nهمچنین اطلاعات بانکی شما و دریافت‌کنندگان پرداخت شما را جمع‌آوری می‌کنیم.")
    SectionTitle("جمع‌آوری و استفاده از اطلاعات شخصی")
    Body("ارزوک اطلاعات شخصی شما را برای ارائه خدمات جمع‌آوری می‌کند. به آدرس ایمیل شما برای ارتباط و ارسال ایترنسفر نیاز داریم. همچنین هویت شما را از طریق مدرک شناسایی معتبر تأیید می‌کنیم. این یک الزام FINTRAC است.")
    SectionTitle("افشای اطلاعات شخصی")
    Body("ممکن است اطلاعات شخصی شما را در صورت نیاز برای انجام کسب‌وکار با سایر طرف‌ها به اشتراک بگذاریم.")
    SectionTitle("سیاست‌های حفاظت از داده")
    Body("برای امنیت حساب شما، رمز عبور هش می‌شود و هیچ‌کس نمی‌تواند رمز عبور اصلی شما را بازیابی کند.\nپایگاه‌های داده ما رمزگذاری شده‌اند. تمام ارتباطات کاربران با ارزوک از طریق پروتکل HTTPS انجام می‌شود و ایمیل‌های تراکنشی نیز با TLS رمزگذاری می‌شوند.")
    SectionTitle("به‌روزرسانی سیاست حریم خصوصی")
    Body("این سیاست حریم خصوصی از تاریخ «آخرین به‌روزرسانی» در پایین صفحه معتبر است. استفاده مستمر شما از پلتفرم ارزوک پس از هر تغییر در سیاست حریم خصوصی، به منزله پذیرش شرایط تجدیدنظرشده است. لطفاً برای هرگونه سؤال با ما تماس بگیرید.")
    Body("\nآخرین به‌روزرسانی: ۲۳ دسامبر ۲۰۲۰")
}

@Composable fun TermsFa() {
    Body("لطفاً این شرایط را با دقت بخوانید. دسترسی به خدمات ارزوک و استفاده از آن‌ها مشروط به موافقت شما با این شرایط است. با ایجاد حساب یا استفاده از وب‌سایت، شما به این شرایط متعهد می‌شوید.")
    FaqItem("۱. مسئولیت حساب‌ها",
        "اگر حسابی در وب‌سایت ایجاد کنید، مسئول حفظ امنیت حساب خود هستید و در قبال تمام فعالیت‌هایی که تحت حساب انجام می‌شود مسئولیت دارید. باید اطلاعات دقیق، به‌روز و کامل ارائه دهید. هرگونه استفاده غیرمجاز از حساب خود را فوراً به ارزوک اطلاع دهید.")
    FaqItem("۲. مسئولیت کاربران وب‌سایت و/یا خدمات",
        "دسترسی و استفاده شما از وب‌سایت و/یا خدمات باید قانونی و مطابق با این شرایط و تمام سیاست‌های ضد پول‌شویی (AML) کشور محل اقامت شما باشد. هرگونه استفاده از وب‌سایت برای فعالیت‌های پول‌شویی کاملاً ممنوع است.")
    FaqItem("۳. کارمزدها و پرداخت‌ها",
        "با فروش یا خرید ارز و انتقال به خارج از کشور، موافقت می‌کنید که کارمزد خدمات را که در زمان ایجاد فروش یا خرید جدید تعیین می‌شود، پرداخت کنید. قیمت‌ها و تنظیمات خدمات ممکن است در هر زمان به صلاحدید ارزوک تغییر کنند.")
    FaqItem("۴. حقوق مالکیت معنوی",
        "ارزوک، لوگوی ارزوک و تمام علائم تجاری مرتبط با وب‌سایت و خدمات، علائم تجاری ثبت‌شده ارزوک هستند. استفاده شما از وب‌سایت هیچ حق یا مجوزی برای بازتولید علائم تجاری ارزوک به شما نمی‌دهد.")
    FaqItem("۵. فسخ",
        "ارزوک می‌تواند هر توافق و دسترسی به حساب شما را در صورتی که خدمات دیگر از نظر قانونی در حوزه قضایی شما در دسترس نباشند یا از نظر تجاری مقرون‌به‌صرفه نباشند، فسخ کند.")
    FaqItem("۶. تغییرات",
        "تنظیمات و مشخصات وب‌سایت و خدمات ممکن است از زمان به زمان به صلاحدید ارزوک اصلاح و/یا به‌روزرسانی شوند. شما به هرگونه تغییر یا به‌روزرسانی متعهد هستید.")
    FaqItem("۷. محدودیت ضمانت‌های ارزوک",
        "ارزوک هیچ ضمانت یا نمایندگی‌ای در مورد دسترسی یا استفاده شما از وب‌سایت و/یا خدمات ارائه نمی‌دهد. اگر از وب‌سایت ناراضی هستید، تنها راه‌حل شما توقف استفاده از وب‌سایت است.")
    FaqItem("۸. محدودیت مسئولیت ارزوک",
        "تحت هیچ شرایطی ارزوک در قبال خسارات غیرمستقیم، تبعی، تصادفی، خاص یا تنبیهی، از جمله از دست دادن سود و وقفه در کسب‌وکار، مسئول نخواهد بود.")
    FaqItem("۹. نمایندگی‌ها و ضمانت‌های شما",
        "شما نمایندگی و ضمانت می‌کنید که استفاده شما از وب‌سایت و/یا خدمات مطابق با هر توافقی بین شما و ارزوک، سیاست حریم خصوصی ارزوک، این شرایط و تمام قوانین و مقررات قابل اجرا خواهد بود.")
    FaqItem("۱۰. غرامت",
        "طرفین موافقت می‌کنند که یکدیگر را در برابر تمام ادعاها، خسارات، تعهدات و هزینه‌های ناشی از نقض این شرایط دفاع کنند، غرامت دهند و بی‌ضرر نگه دارند.")
    FaqItem("۱۱. متفرقه",
        "هر طرف مسئول حفظ اقدامات فنی و سازمانی مناسب برای حفاظت از داده‌های پردازش‌شده در سیستم‌های خود است.\nارزوک در قبال هیچ تأخیر یا عدم انجام تعهداتش که ناشی از رویدادهای خارج از کنترل معقول آن باشد، مسئول نخواهد بود.")
}

@Composable fun ContactUsContent() {
    ContactUsForm(
        categories = listOf("Feedback", "Complaints", "Deposit Details"),
        labels = mapOf(
            "category" to "Category", "name" to "Name", "email" to "Email",
            "phone" to "Phone", "subject" to "Subject", "body" to "Body",
            "submit" to "Submit", "phoneError" to "Correct phone number has 10 digits",
            "emailError" to "Email is not correct"
        )
    )
}

@Composable fun ContactUsFa() {
    ContactUsForm(
        categories = listOf("بازخورد", "شکایات", "جزئیات واریز"),
        labels = mapOf(
            "category" to "دسته‌بندی", "name" to "نام", "email" to "ایمیل",
            "phone" to "تلفن", "subject" to "موضوع", "body" to "متن پیام",
            "submit" to "ارسال", "phoneError" to "شماره تلفن باید ۱۰ رقم باشد",
            "emailError" to "ایمیل صحیح نیست"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactUsForm(categories: List<String>, labels: Map<String, String>) {
    var category by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val isEmailValid = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }
    val canSubmit = category.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty() &&
            phone.isNotEmpty() && subject.isNotEmpty() && body.isNotEmpty()

    if (submitted) {
        Text("✓ ${labels["submit"]}", fontWeight = FontWeight.Bold, fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp))
        return
    }

    if (phoneError) Text(labels["phoneError"] ?: "", color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(bottom = 4.dp))
    if (emailError) Text(labels["emailError"] ?: "", color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(bottom = 4.dp))

    // Category dropdown
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text(labels["category"] ?: "", fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    listOf(
        Triple(name, { v: String -> name = v }, labels["name"] ?: ""),
        Triple(email, { v: String -> email = v }, labels["email"] ?: ""),
        Triple(phone, { v: String -> phone = v }, labels["phone"] ?: ""),
        Triple(subject, { v: String -> subject = v }, labels["subject"] ?: ""),
    ).forEach { (value, onChange, label) ->
        OutlinedTextField(
            value = value, onValueChange = onChange,
            label = { Text(label, fontSize = 10.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
    }
    OutlinedTextField(
        value = body, onValueChange = { body = it },
        label = { Text(labels["body"] ?: "", fontSize = 10.sp) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
    ArzookButton(
        onClick = {
            emailError = !isEmailValid
            phoneError = !isPhoneValid
            if (isEmailValid && isPhoneValid) submitted = true
        },
        enabled = canSubmit
    ) { Text(labels["submit"] ?: "") }
}