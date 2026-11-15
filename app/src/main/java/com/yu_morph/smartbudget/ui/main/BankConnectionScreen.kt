@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.auth.PrimaryAuthButton
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen

private data class BankOption(val key: String, @param:StringRes val nameResource: Int, val color: Color)

private val BankOptions =
    listOf(
        BankOption("kb", R.string.bank_kb, Color(0xFFFFB800)),
        BankOption("shinhan", R.string.bank_shinhan, Color(0xFF2565B7)),
        BankOption("hana", R.string.bank_hana, Color(0xFF008B80)),
        BankOption("woori", R.string.bank_woori, Color(0xFF1688D4)),
        BankOption("nh", R.string.bank_nh, Color(0xFF19A164)),
        BankOption("ibk", R.string.bank_ibk, Color(0xFF2171B9)),
        BankOption("kakao", R.string.bank_kakao, Color(0xFFFFC928)),
        BankOption("toss", R.string.bank_toss, Color(0xFF316FEE)),
        BankOption("kbank", R.string.bank_kbank, Color(0xFF6B49D8)),
        BankOption("busan", R.string.bank_busan, Color(0xFFD52836)),
        BankOption("sc", R.string.bank_sc, Color(0xFF218C74)),
        BankOption("suhyup", R.string.bank_suhyup, Color(0xFF1672B8)),
    )

@Composable
internal fun BankConnectionScreen(isLoading: Boolean, modifier: Modifier = Modifier, onConnect: () -> Unit) {
    val selectedKeys = remember { mutableStateListOf<String>().apply { addAll(BankOptions.map { it.key }) } }
    if (isLoading) {
        BankSyncLoading(modifier)
        return
    }
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedKeys.size == BankOptions.size) {
                            selectedKeys.clear()
                        } else {
                            selectedKeys.clear()
                            selectedKeys.addAll(BankOptions.map { it.key })
                        }
                    }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_bank_select_all),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            SelectionCheck(selectedKeys.size == BankOptions.size)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(BankOptions, key = { it.key }) { bank ->
                BankItem(
                    bank = bank,
                    selected = bank.key in selectedKeys,
                    onClick = {
                        if (bank.key in selectedKeys) selectedKeys.remove(bank.key) else selectedKeys.add(bank.key)
                    },
                )
            }
        }
        PrimaryAuthButton(
            text = stringResource(R.string.settings_bank_connect),
            enabled = selectedKeys.isNotEmpty(),
            onClick = onConnect,
        )
    }
}

@Composable
private fun BankItem(bank: BankOption, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (selected) SmartBudgetGreen else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).background(bank.color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bank_logo),
                    contentDescription = stringResource(R.string.settings_bank_logo, stringResource(bank.nameResource)),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = stringResource(bank.nameResource),
                modifier = Modifier.padding(start = 10.dp).weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            SelectionCheck(selected)
        }
    }
}

@Composable
private fun SelectionCheck(selected: Boolean) {
    Box(
        modifier =
            Modifier
                .size(22.dp)
                .background(if (selected) SmartBudgetGreen else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun BankSyncLoading(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.bank_sync_loading),
            contentDescription = null,
            modifier = Modifier.size(156.dp),
        )
        CircularProgressIndicator(
            modifier = Modifier.padding(top = 18.dp).size(28.dp),
            color = SmartBudgetGreen,
            strokeWidth = 3.dp,
        )
        Text(
            text = stringResource(R.string.settings_bank_fetching),
            modifier = Modifier.padding(top = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )
    }
}
