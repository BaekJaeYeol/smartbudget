@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caverock.androidsvg.SVG
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.auth.PrimaryAuthButton
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen

private data class BankOption(
    val key: String,
    @param:StringRes val nameResource: Int,
    val logoAssetName: String,
    val logoWidth: Int,
    val logoHeight: Int,
)

private val BankOptions =
    listOf(
        BankOption("kb", R.string.bank_kb, "KB국민은행 심볼.svg", 30, 24),
        BankOption("shinhan", R.string.bank_shinhan, "신한은행 심볼.svg", 28, 28),
        BankOption("hana", R.string.bank_hana, "하나은행 심볼.svg", 27, 28),
        BankOption("woori", R.string.bank_woori, "우리은행 심볼.svg", 28, 28),
        BankOption("nh", R.string.bank_nh, "NH농협은행 심볼.svg", 23, 29),
        BankOption("ibk", R.string.bank_ibk, "IBK기업은행 심볼.svg", 28, 28),
        BankOption("kakao", R.string.bank_kakao, "카카오뱅크 심볼.svg", 28, 28),
        BankOption("toss", R.string.bank_toss, "토스뱅크 심볼.svg", 28, 27),
        BankOption("kbank", R.string.bank_kbank, "케이뱅크 심볼.svg", 32, 14),
        BankOption("busan", R.string.bank_busan, "BNK부산은행 심볼.svg", 32, 12),
        BankOption("sc", R.string.bank_sc, "SC제일은행 심볼.svg", 19, 30),
        BankOption("suhyup", R.string.bank_suhyup, "수협은행 심볼.svg", 30, 22),
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
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                SvgAssetImage(
                    assetName = bank.logoAssetName,
                    contentDescription = stringResource(R.string.settings_bank_logo, stringResource(bank.nameResource)),
                    modifier = Modifier.size(bank.logoWidth.dp, bank.logoHeight.dp),
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
private fun SvgAssetImage(assetName: String, contentDescription: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val image = remember(assetName) {
        context.assets.open(assetName).use { input ->
            val picture = SVG.getFromInputStream(input).renderToPicture(SVG_RENDER_SIZE, SVG_RENDER_SIZE)
            Bitmap.createBitmap(SVG_RENDER_SIZE, SVG_RENDER_SIZE, Bitmap.Config.ARGB_8888).also { bitmap ->
                Canvas(bitmap).drawPicture(picture)
            }.asImageBitmap()
        }
    }
    Image(
        bitmap = image,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

private const val SVG_RENDER_SIZE = 256

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
