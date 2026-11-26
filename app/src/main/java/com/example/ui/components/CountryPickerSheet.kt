package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LocalStrings

data class CountryCode(
    val name: String,
    val dialCode: String,
    val iso: String,
    val flag: String,
)

val PopularCountries = listOf(
    CountryCode("Oʻzbekiston", "+998", "UZ", "🇺🇿"),
    CountryCode("Rossiya", "+7", "RU", "🇷🇺"),
    CountryCode("Qozogʻiston", "+7", "KZ", "🇰🇿"),
    CountryCode("Qirgʻiziston", "+996", "KG", "🇰🇬"),
    CountryCode("Tojikiston", "+992", "TJ", "🇹🇯"),
    CountryCode("Turkmaniston", "+993", "TM", "🇹🇲"),
    CountryCode("Turkiya", "+90", "TR", "🇹🇷"),
    CountryCode("AQSH", "+1", "US", "🇺🇸"),
    CountryCode("Buyuk Britaniya", "+44", "GB", "🇬🇧"),
    CountryCode("Germaniya", "+49", "DE", "🇩🇪"),
    CountryCode("Janubiy Koreya", "+82", "KR", "🇰🇷"),
    CountryCode("BAA", "+971", "AE", "🇦🇪"),
    CountryCode("Ukraina", "+380", "UA", "🇺🇦"),
    CountryCode("Hindiston", "+91", "IN", "🇮🇳"),
    CountryCode("Fransiya", "+33", "FR", "🇫🇷"),
    CountryCode("Italiya", "+39", "IT", "🇮🇹"),
    CountryCode("Yaponiya", "+81", "JP", "🇯🇵"),
    CountryCode("Xitoy", "+86", "CN", "🇨🇳"),
    CountryCode("Kanada", "+1", "CA", "🇨🇦"),
    CountryCode("Braziliya", "+55", "BR", "🇧🇷"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onSelect: (CountryCode) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) PopularCountries
        else PopularCountries.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.dialCode.contains(query) ||
                it.iso.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = strings.chooseCountry,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.searchCountry) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("country_search_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(filtered, key = { "${it.iso}_${it.dialCode}" }) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = country.flag, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = country.dialCode,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
