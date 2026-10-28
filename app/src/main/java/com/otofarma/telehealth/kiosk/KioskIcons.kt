package com.otofarma.telehealth.kiosk

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun AreaMark(glyph: AreaGlyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (glyph) {
            AreaGlyph.Cross -> cross(tint)
            AreaGlyph.Camera -> camera(tint)
            AreaGlyph.Person -> person(tint)
            AreaGlyph.Desk -> desk(tint)
        }
    }
}

@Composable
internal fun SpecialtyMark(glyph: SpecialtyGlyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (glyph) {
            SpecialtyGlyph.Heart -> heart(tint)
            SpecialtyGlyph.Face -> face(tint)
            SpecialtyGlyph.Tooth -> tooth(tint)
            SpecialtyGlyph.Headphones -> headphones(tint)
        }
    }
}

@Composable
internal fun ServiceMark(glyph: ServiceGlyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (glyph) {
            ServiceGlyph.Glass -> glass(tint)
            ServiceGlyph.Wave -> wave(tint)
            ServiceGlyph.Page -> page(tint)
        }
    }
}

@Composable
internal fun FingerMark(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { finger(tint) }
}

@Composable
internal fun PlayMark(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { play(tint) }
}

@Composable
internal fun PhotoMark(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { photo(tint) }
}

private fun DrawScope.pen(width: Float = 0.085f): Stroke =
    Stroke(
        width = size.minDimension * width,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )

private fun DrawScope.cross(tint: Color) {
    val arm = size.minDimension * 0.16f
    val len = size.minDimension * 0.62f
    drawRoundRect(
        tint,
        topLeft = Offset(center.x - arm / 2f, center.y - len / 2f),
        size = Size(arm, len),
        cornerRadius = CornerRadius(arm / 2f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(center.x - len / 2f, center.y - arm / 2f),
        size = Size(len, arm),
        cornerRadius = CornerRadius(arm / 2f),
    )
}

private fun DrawScope.camera(tint: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.08f, h * 0.30f),
        size = Size(w * 0.52f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.08f),
    )
    val lens = Path().apply {
        moveTo(w * 0.64f, h * 0.36f)
        lineTo(w * 0.90f, h * 0.24f)
        lineTo(w * 0.90f, h * 0.78f)
        lineTo(w * 0.64f, h * 0.66f)
        close()
    }
    drawPath(lens, tint)
}

private fun DrawScope.person(tint: Color) {
    val w = size.width
    val h = size.height
    drawCircle(tint, radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.28f))
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.22f, h * 0.52f),
        size = Size(w * 0.56f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.28f),
    )
}

private fun DrawScope.desk(tint: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.30f, h * 0.16f),
        size = Size(w * 0.40f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.06f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.10f, h * 0.48f),
        size = Size(w * 0.80f, h * 0.14f),
        cornerRadius = CornerRadius(w * 0.04f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.18f, h * 0.64f),
        size = Size(w * 0.10f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.03f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.72f, h * 0.64f),
        size = Size(w * 0.10f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.03f),
    )
}

private fun DrawScope.heart(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.84f)
        cubicTo(w * 0.18f, h * 0.62f, w * 0.10f, h * 0.40f, w * 0.22f, h * 0.26f)
        cubicTo(w * 0.32f, h * 0.14f, w * 0.46f, h * 0.20f, w * 0.50f, h * 0.32f)
        cubicTo(w * 0.54f, h * 0.20f, w * 0.68f, h * 0.14f, w * 0.78f, h * 0.26f)
        cubicTo(w * 0.90f, h * 0.40f, w * 0.82f, h * 0.62f, w * 0.50f, h * 0.84f)
        close()
    }
    drawPath(path, tint)
}

private fun DrawScope.face(tint: Color) {
    val w = size.width
    val h = size.height
    drawCircle(
        tint,
        radius = w * 0.38f,
        center = Offset(w * 0.50f, h * 0.50f),
        style = pen(0.07f),
    )
    drawCircle(tint, radius = w * 0.045f, center = Offset(w * 0.36f, h * 0.42f))
    drawCircle(tint, radius = w * 0.045f, center = Offset(w * 0.64f, h * 0.42f))
    drawArc(
        color = tint,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.32f, h * 0.46f),
        size = Size(w * 0.36f, h * 0.28f),
        style = pen(0.07f),
    )
}

private fun DrawScope.tooth(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.28f, h * 0.22f)
        cubicTo(w * 0.28f, h * 0.08f, w * 0.72f, h * 0.08f, w * 0.72f, h * 0.22f)
        cubicTo(w * 0.92f, h * 0.24f, w * 0.90f, h * 0.48f, w * 0.74f, h * 0.52f)
        lineTo(w * 0.68f, h * 0.86f)
        cubicTo(w * 0.64f, h * 0.98f, w * 0.54f, h * 0.90f, w * 0.52f, h * 0.70f)
        lineTo(w * 0.48f, h * 0.70f)
        cubicTo(w * 0.46f, h * 0.90f, w * 0.36f, h * 0.98f, w * 0.32f, h * 0.86f)
        lineTo(w * 0.26f, h * 0.52f)
        cubicTo(w * 0.10f, h * 0.48f, w * 0.08f, h * 0.24f, w * 0.28f, h * 0.22f)
        close()
    }
    drawPath(path, tint)
}

private fun DrawScope.headphones(tint: Color) {
    val w = size.width
    val h = size.height
    drawArc(
        color = tint,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.20f, h * 0.14f),
        size = Size(w * 0.60f, h * 0.56f),
        style = pen(0.09f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.12f, h * 0.46f),
        size = Size(w * 0.20f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.08f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.68f, h * 0.46f),
        size = Size(w * 0.20f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.08f),
    )
}

private fun DrawScope.glass(tint: Color) {
    val w = size.width
    val h = size.height
    drawCircle(
        tint,
        radius = w * 0.26f,
        center = Offset(w * 0.42f, h * 0.42f),
        style = pen(0.09f),
    )
    drawLine(
        tint,
        start = Offset(w * 0.60f, h * 0.60f),
        end = Offset(w * 0.84f, h * 0.84f),
        strokeWidth = size.minDimension * 0.09f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.wave(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.06f, h * 0.58f)
        lineTo(w * 0.26f, h * 0.58f)
        lineTo(w * 0.36f, h * 0.28f)
        lineTo(w * 0.50f, h * 0.74f)
        lineTo(w * 0.62f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.58f)
        lineTo(w * 0.94f, h * 0.58f)
    }
    drawPath(path, tint, style = pen(0.08f))
}

private fun DrawScope.page(tint: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.22f, h * 0.10f),
        size = Size(w * 0.56f, h * 0.80f),
        cornerRadius = CornerRadius(w * 0.06f),
        style = pen(0.08f),
    )
    val ink = pen(0.07f)
    drawLine(tint, Offset(w * 0.34f, h * 0.36f), Offset(w * 0.66f, h * 0.36f), ink.width, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.34f, h * 0.50f), Offset(w * 0.66f, h * 0.50f), ink.width, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.34f, h * 0.64f), Offset(w * 0.54f, h * 0.64f), ink.width, StrokeCap.Round)
}

private fun DrawScope.finger(tint: Color) {
    val w = size.width
    val h = size.height
    drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.46f, h * 0.20f))
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.34f, h * 0.22f),
        size = Size(w * 0.24f, h * 0.40f),
        cornerRadius = CornerRadius(w * 0.12f),
    )
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.22f, h * 0.50f),
        size = Size(w * 0.52f, h * 0.34f),
        cornerRadius = CornerRadius(w * 0.16f),
    )
}

private fun DrawScope.play(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.78f, h * 0.50f)
        lineTo(w * 0.36f, h * 0.78f)
        close()
    }
    drawPath(path, tint)
}

private fun DrawScope.photo(tint: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        tint,
        topLeft = Offset(w * 0.10f, h * 0.18f),
        size = Size(w * 0.80f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = pen(0.08f),
    )
    drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.32f, h * 0.38f))
    val hill = Path().apply {
        moveTo(w * 0.20f, h * 0.70f)
        lineTo(w * 0.40f, h * 0.50f)
        lineTo(w * 0.54f, h * 0.64f)
        lineTo(w * 0.68f, h * 0.46f)
        lineTo(w * 0.80f, h * 0.70f)
        close()
    }
    drawPath(hill, tint)
}
