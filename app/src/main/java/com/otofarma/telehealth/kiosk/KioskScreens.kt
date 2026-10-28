package com.otofarma.telehealth.kiosk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.otofarma.telehealth.R

private val CardShape = RoundedCornerShape(20.dp)
private val BarShape = RoundedCornerShape(24.dp)

@Composable
internal fun AreaScreen(areas: List<KioskArea>, onPick: (KioskArea) -> Unit) {
    Column(Modifier.fillMaxSize().background(KioskMist)) {
        Box(
            Modifier.fillMaxWidth().height(156.dp).background(KioskWhite),
            contentAlignment = Alignment.Center,
        ) {
            OtofarmaLogo(height = 104.dp)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            HintRow(title = "Scegli un'area", body = "Tocca una di queste.")
            Spacer(Modifier.height(16.dp))
            ChoiceGrid(areas, Modifier.weight(1f)) { area ->
                ChoiceCard(
                    title = area.title,
                    subtitle = area.subtitle,
                    disc = area.glyph.disc(),
                    onClick = { onPick(area) },
                    mark = {
                        AreaMark(area.glyph, area.glyph.icon(), Modifier.fillMaxSize())
                    },
                )
            }
        }
    }
}

@Composable
internal fun SpecialtyScreen(
    area: KioskArea,
    onBack: () -> Unit,
    onPick: (KioskSpecialty) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(KioskMist)) {
        GreenHeader(
            title = area.title,
            hint = if (area.specialties.isEmpty()) "Tocca Indietro." else "Tocca una di queste.",
            onBack = onBack,
        )
        if (area.specialties.isEmpty()) {
            EmptyLine()
        } else {
            ChoiceGrid(
                area.specialties,
                Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 20.dp),
            ) { specialty ->
                ChoiceCard(
                    title = specialty.title,
                    subtitle = specialty.subtitle,
                    disc = specialty.glyph.disc(),
                    onClick = { onPick(specialty) },
                    mark = {
                        SpecialtyMark(specialty.glyph, specialty.glyph.icon(), Modifier.fillMaxSize())
                    },
                )
            }
        }
    }
}

@Composable
internal fun ServiceScreen(
    specialty: KioskSpecialty,
    onBack: () -> Unit,
    onPick: (KioskService) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(KioskMist)) {
        LightBar()
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            TitleRow(title = specialty.title, onBack = onBack)
            Spacer(Modifier.height(8.dp))
            if (specialty.services.isEmpty()) {
                HintRow(title = "Tocca Indietro.", body = "Qui non c'è ancora una scelta.")
                EmptyLine()
            } else {
                HintRow(title = "Metti il dito su un colore.", body = "Tocca una riga.")
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    specialty.services.forEach { service ->
                        ServiceBar(service, Modifier.weight(1f)) { onPick(service) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun OpenScreen(
    service: KioskService,
    onBack: () -> Unit,
    onOpen: () -> Boolean,
) {
    var blocked by remember(service.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(KioskMist)) {
        LightBar(logoHeight = 72.dp)
        TitleRow(
            title = service.title,
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            service.blocks.forEachIndexed { index, block ->
                SeesawRow(block, mediaOnLeft = index % 2 == 0)
            }
        }
        if (blocked) {
            Text(
                "Non si apre. Chiedi a chi ti aiuta.",
                color = KioskInk,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF4C2))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        OpenButton(service.buttonTitle, service.buttonSubtitle) {
            blocked = !onOpen()
        }
    }
}

@Composable
private fun LightBar(logoHeight: Dp = 84.dp) {
    Box(
        Modifier.fillMaxWidth().background(KioskWhite).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        OtofarmaLogo(height = logoHeight)
    }
}

@Composable
private fun GreenHeader(title: String, hint: String, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(KioskGreen)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackChip(onGreen = true, onClick = onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                OtofarmaLogo(height = 36.dp, onPlate = true)
            }
            Spacer(Modifier.width(148.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Sei nella pagina dopo.",
            color = KioskWhite,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
        Text(
            title,
            color = KioskWhite,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            hint,
            color = KioskWhite,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
private fun TitleRow(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BackChip(onGreen = false, onClick = onBack)
        Text(
            title,
            color = KioskInk,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp).weight(1f),
        )
    }
}

@Composable
private fun BackChip(onGreen: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (onGreen) KioskWhite else KioskGreenSoft,
        shape = RoundedCornerShape(23.dp),
        modifier = Modifier.height(46.dp).width(148.dp),
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                "‹",
                color = if (onGreen) KioskInk else KioskGreen,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Indietro",
                color = if (onGreen) KioskInk else KioskGreen,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun OtofarmaLogo(height: Dp, onPlate: Boolean = false) {
    if (onPlate) {
        Surface(color = KioskWhite, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                LogoImage(height)
            }
        }
    } else {
        LogoImage(height)
    }
}

@Composable
private fun LogoImage(height: Dp) {
    Image(
        painter = painterResource(R.drawable.otofarma_logo),
        contentDescription = "Otofarma",
        contentScale = ContentScale.Fit,
        modifier = Modifier.height(height).aspectRatio(3f, matchHeightConstraintsFirst = true),
    )
}

@Composable
private fun HintRow(title: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(KioskGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            FingerMark(KioskGreen, Modifier.size(34.dp))
        }
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                title,
                color = KioskInk,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                body,
                color = KioskMuted,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            )
        }
    }
}

@Composable
private fun <T> ChoiceGrid(
    items: List<T>,
    modifier: Modifier,
    card: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { item ->
                    Box(Modifier.weight(1f).fillMaxHeight()) { card(item) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    disc: Color,
    onClick: () -> Unit,
    mark: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .shadow(6.dp, CardShape)
            .clip(CardShape)
            .background(KioskWhite)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(disc),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(36.dp)) { mark() }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            color = KioskInk,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            subtitle,
            color = KioskMuted,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        ToccaPill(KioskGreenSoft, KioskGreen)
    }
}

@Composable
private fun ServiceBar(service: KioskService, modifier: Modifier, onClick: () -> Unit) {
    val paint = service.tone.paint()
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 108.dp)
            .shadow(6.dp, BarShape)
            .clip(BarShape)
            .background(paint.bar)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(KioskWhite),
            contentAlignment = Alignment.Center,
        ) {
            ServiceMark(service.glyph, paint.icon, Modifier.size(36.dp))
        }
        Text(
            service.title,
            color = paint.onBar,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 18.dp),
        )
        ToccaPill(KioskWhite, KioskInk)
    }
}

@Composable
private fun ToccaPill(background: Color, text: Color) {
    Text(
        "Tocca",
        color = text,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun SeesawRow(block: KioskBlock, mediaOnLeft: Boolean) {
    Row(
        Modifier.fillMaxWidth().height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (mediaOnLeft) {
            MediaPane(block, Modifier.weight(1f).fillMaxHeight())
            CopyPane(block, Modifier.weight(1f).fillMaxHeight())
        } else {
            CopyPane(block, Modifier.weight(1f).fillMaxHeight())
            MediaPane(block, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun CopyPane(block: KioskBlock, modifier: Modifier) {
    Column(
        modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            block.kicker,
            color = KioskGreen,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            block.title,
            color = KioskInk,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            block.body,
            color = KioskMuted,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
private fun MediaPane(block: KioskBlock, modifier: Modifier) {
    val dark = block.mediaKind == MediaKind.Video
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(if (dark) KioskGreenDeep else KioskGreenSoft)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (dark) {
            Box(
                Modifier.size(72.dp).clip(CircleShape).background(KioskWhite),
                contentAlignment = Alignment.Center,
            ) {
                PlayMark(KioskGreenDeep, Modifier.size(32.dp))
            }
        } else {
            PhotoMark(KioskGreen, Modifier.size(64.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            block.mediaLabel,
            color = if (dark) KioskWhite else KioskGreen,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (dark) Color.White.copy(alpha = 0.16f) else KioskWhite)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            block.mediaCaption,
            color = if (dark) KioskWhite else KioskInk,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OpenButton(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(148.dp)
            .background(KioskYellow)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title,
            color = KioskInk,
            fontSize = 40.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            subtitle,
            color = KioskInk,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ColumnScope.EmptyLine() {
    Box(Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            "Qui non c'è ancora niente da toccare.",
            color = KioskInk,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

private data class BarPaint(val bar: Color, val onBar: Color, val icon: Color)

private fun ServiceTone.paint(): BarPaint = when (this) {
    ServiceTone.Green -> BarPaint(Color(0xFF3AAA3A), KioskWhite, KioskGreenDeep)
    ServiceTone.Yellow -> BarPaint(KioskYellow, KioskInk, Color(0xFF8A6A00))
    ServiceTone.Red -> BarPaint(Color(0xFFE23B3B), KioskWhite, Color(0xFF8E1E1E))
}

private fun AreaGlyph.disc(): Color = when (this) {
    AreaGlyph.Cross -> KioskGreenSoft
    AreaGlyph.Camera -> Color(0xFFE5F4F8)
    AreaGlyph.Person -> Color(0xFFFFF1E4)
    AreaGlyph.Desk -> Color(0xFFF3EEF8)
}

private fun AreaGlyph.icon(): Color = when (this) {
    AreaGlyph.Cross -> KioskGreen
    AreaGlyph.Camera -> Color(0xFF1F6B8A)
    AreaGlyph.Person -> Color(0xFFC46B2C)
    AreaGlyph.Desk -> Color(0xFF6A4C93)
}

private fun SpecialtyGlyph.disc(): Color = when (this) {
    SpecialtyGlyph.Heart -> Color(0xFFFDE8EC)
    SpecialtyGlyph.Face -> Color(0xFFFFF1E4)
    SpecialtyGlyph.Tooth -> Color(0xFFE5F4F8)
    SpecialtyGlyph.Headphones -> KioskGreenSoft
}

private fun SpecialtyGlyph.icon(): Color = when (this) {
    SpecialtyGlyph.Heart -> Color(0xFFC43B4E)
    SpecialtyGlyph.Face -> Color(0xFFC46B2C)
    SpecialtyGlyph.Tooth -> Color(0xFF1F6B8A)
    SpecialtyGlyph.Headphones -> KioskGreen
}
