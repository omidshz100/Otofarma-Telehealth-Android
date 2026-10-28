package com.otofarma.telehealth.kiosk

/**
 * The four steps before an exam app opens.
 *
 * Visita online, Accoglienza, and Sportello are still guesses from an unreadable
 * photo. Only Specialistiche was readable. Audiologia is the only specialty with
 * services so far. Package names stay empty until the real exam apps are known.
 */
internal object KioskCatalog {
    val areas: List<KioskArea> = listOf(
        KioskArea(
            id = "specialistiche",
            title = "Specialistiche",
            subtitle = "Esami",
            glyph = AreaGlyph.Cross,
            specialties = listOf(
                KioskSpecialty(
                    id = "cardiologia",
                    title = "Cardiologia",
                    subtitle = "Cuore",
                    glyph = SpecialtyGlyph.Heart,
                ),
                KioskSpecialty(
                    id = "dermatologia",
                    title = "Dermatologia",
                    subtitle = "Pelle",
                    glyph = SpecialtyGlyph.Face,
                ),
                KioskSpecialty(
                    id = "odontoiatria",
                    title = "Odontoiatria",
                    subtitle = "Denti",
                    glyph = SpecialtyGlyph.Tooth,
                ),
                KioskSpecialty(
                    id = "audiologia",
                    title = "Audiologia",
                    subtitle = "Udito",
                    glyph = SpecialtyGlyph.Headphones,
                    services = listOf(
                        hearingService(
                            id = "otoscopio",
                            title = "Esame otoscopico",
                            tone = ServiceTone.Green,
                            glyph = ServiceGlyph.Glass,
                            purposeTitle = "Guarda l'orecchio.",
                            purposeBody = "Il video mostra come si guarda dentro l'orecchio. Guardi, poi fai lo stesso.",
                            buttonSubtitle = "Avvia otoscopia",
                        ),
                        hearingService(
                            id = "audiometria",
                            title = "Esame audiometrico",
                            tone = ServiceTone.Yellow,
                            glyph = ServiceGlyph.Wave,
                            purposeTitle = "Controlla come senti.",
                            purposeBody = "Il video mostra l'esame, dal suono grave al suono acuto. Guardi, poi fai lo stesso.",
                            buttonSubtitle = "Avvia teleaudiologia",
                        ),
                        hearingService(
                            id = "prescrizione",
                            title = "Prescrizione",
                            tone = ServiceTone.Red,
                            glyph = ServiceGlyph.Page,
                            purposeTitle = "Il foglio del medico.",
                            purposeBody = "Qui si apre la prescrizione. Il video mostra i passi.",
                            buttonSubtitle = "Apri la prescrizione",
                            actionTitle = "Premi il pulsante giallo.",
                            actionBody = "Si apre l'app della prescrizione.",
                            photoCaption = "Il foglio",
                        ),
                    ),
                ),
            ),
        ),
        KioskArea(
            id = "visita",
            title = "Visita online",
            subtitle = "Medico",
            glyph = AreaGlyph.Camera,
        ),
        KioskArea(
            id = "accoglienza",
            title = "Accoglienza",
            subtitle = "Ingresso",
            glyph = AreaGlyph.Person,
        ),
        KioskArea(
            id = "sportello",
            title = "Sportello",
            subtitle = "Banco",
            glyph = AreaGlyph.Desk,
        ),
    )

    fun area(id: String): KioskArea = areas.first { it.id == id }

    fun specialty(areaId: String, specialtyId: String): KioskSpecialty =
        area(areaId).specialties.first { it.id == specialtyId }

    fun service(id: String): KioskService =
        areas.flatMap { it.specialties }.flatMap { it.services }.first { it.id == id }
}

internal enum class AreaGlyph { Cross, Camera, Person, Desk }

internal enum class SpecialtyGlyph { Heart, Face, Tooth, Headphones }

internal enum class ServiceGlyph { Glass, Wave, Page }

internal enum class ServiceTone { Green, Yellow, Red }

internal enum class MediaKind { Video, Photo }

internal data class KioskArea(
    val id: String,
    val title: String,
    val subtitle: String,
    val glyph: AreaGlyph,
    val specialties: List<KioskSpecialty> = emptyList(),
)

internal data class KioskSpecialty(
    val id: String,
    val title: String,
    val subtitle: String,
    val glyph: SpecialtyGlyph,
    val services: List<KioskService> = emptyList(),
)

internal data class KioskService(
    val id: String,
    val title: String,
    val tone: ServiceTone,
    val glyph: ServiceGlyph,
    val blocks: List<KioskBlock>,
    val buttonTitle: String,
    val buttonSubtitle: String,
    val packageName: String? = null,
)

internal data class KioskBlock(
    val kicker: String,
    val title: String,
    val body: String,
    val mediaKind: MediaKind,
    val mediaLabel: String,
    val mediaCaption: String,
)

private fun hearingService(
    id: String,
    title: String,
    tone: ServiceTone,
    glyph: ServiceGlyph,
    purposeTitle: String,
    purposeBody: String,
    buttonSubtitle: String,
    actionTitle: String = "Prendi lo strumento.",
    actionBody: String = "Nella foto la freccia indica cosa prendere dalla valigetta. Poi premi il pulsante giallo.",
    photoCaption: String = "Lo strumento da prendere",
): KioskService = KioskService(
    id = id,
    title = title,
    tone = tone,
    glyph = glyph,
    buttonTitle = "Apri l'app",
    buttonSubtitle = buttonSubtitle,
    blocks = listOf(
        KioskBlock(
            kicker = "A cosa serve",
            title = purposeTitle,
            body = purposeBody,
            mediaKind = MediaKind.Video,
            mediaLabel = "Video",
            mediaCaption = "Come funziona l'esame",
        ),
        KioskBlock(
            kicker = "Cosa fare",
            title = actionTitle,
            body = actionBody,
            mediaKind = MediaKind.Photo,
            mediaLabel = "Foto",
            mediaCaption = photoCaption,
        ),
    ),
)
