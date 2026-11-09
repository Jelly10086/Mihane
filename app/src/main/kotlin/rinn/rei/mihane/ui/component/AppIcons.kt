package rinn.rei.mihane.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Google Material Symbols Rounded, Apache-2.0. Kept offline in the application. */
object AppIcons {
    val Add: ImageVector by lazy {
        ImageVector.Builder("Add", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M440-440H240q-17 0-28.5-11.5T200-480q0-17 11.5-28.5T240-520h200v-200q0-17 11.5-28.5T480-760q17 0 28.5 11.5T520-720v200h200q17 0 28.5 11.5T760-480q0 17-11.5 28.5T720-440H520v200q0 17-11.5 28.5T480-200q-17 0-28.5-11.5T440-240v-200Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val ArrowRight: ImageVector by lazy {
        ImageVector.Builder("ArrowRight", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M504-480 348-636q-11-11-11-28t11-28q11-11 28-11t28 11l184 184q6 6 8.5 13t2.5 15q0 8-2.5 15t-8.5 13L404-268q-11 11-28 11t-28-11q-11-11-11-28t11-28l156-156Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Back: ImageVector by lazy {
        ImageVector.Builder("Back", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m313-440 196 196q12 12 11.5 28T508-188q-12 11-28 11.5T452-188L188-452q-6-6-8.5-13t-2.5-15q0-8 2.5-15t8.5-13l264-264q11-11 27.5-11t28.5 11q12 12 12 28.5T508-715L313-520h447q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H313Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Backup: ImageVector by lazy {
        ImageVector.Builder("Backup", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m440-446-36 35q-11 11-27.5 11T348-412q-11-11-11-28t11-28l104-104q12-12 28-12t28 12l104 104q11 11 11.5 27.5T612-412q-11 11-27.5 11.5T556-411l-36-35v206h220q42 0 71-29t29-71q0-42-29-71t-71-29h-60v-80q0-83-58.5-141.5T480-720q-83 0-141.5 58.5T280-520h-20q-58 0-99 41t-41 99q0 58 41 99t99 41h60q17 0 28.5 11.5T360-200q0 17-11.5 28.5T320-160h-60q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H520q-33 0-56.5-23.5T440-240v-206Zm40 6Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val CheckCircleOutline: ImageVector by lazy {
        ImageVector.Builder("CheckCircleOutline", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m424-408-86-86q-11-11-28-11t-28 11q-11 11-11 28t11 28l114 114q12 12 28 12t28-12l226-226q11-11 11-28t-11-28q-11-11-28-11t-28 11L424-408Zm56 328q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Close: ImageVector by lazy {
        ImageVector.Builder("Close", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Delete: ImageVector by lazy {
        ImageVector.Builder("Delete", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M280-120q-33 0-56.5-23.5T200-200v-520q-17 0-28.5-11.5T160-760q0-17 11.5-28.5T200-800h160q0-17 11.5-28.5T400-840h160q17 0 28.5 11.5T600-800h160q17 0 28.5 11.5T800-760q0 17-11.5 28.5T760-720v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM400-280q17 0 28.5-11.5T440-320v-280q0-17-11.5-28.5T400-640q-17 0-28.5 11.5T360-600v280q0 17 11.5 28.5T400-280Zm160 0q17 0 28.5-11.5T600-320v-280q0-17-11.5-28.5T560-640q-17 0-28.5 11.5T520-600v280q0 17 11.5 28.5T560-280ZM280-720v520-520Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Home: ImageVector by lazy {
        ImageVector.Builder("Home", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M240-200h120v-200q0-17 11.5-28.5T400-440h160q17 0 28.5 11.5T600-400v200h120v-360L480-740 240-560v360Zm-80 0v-360q0-19 8.5-36t23.5-28l240-180q21-16 48-16t48 16l240 180q15 11 23.5 28t8.5 36v360q0 33-23.5 56.5T720-120H560q-17 0-28.5-11.5T520-160v-200h-80v200q0 17-11.5 28.5T400-120H240q-33 0-56.5-23.5T160-200Zm320-270Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Import: ImageVector by lazy {
        ImageVector.Builder("Import", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h287q16 0 30.5 6t25.5 17l194 194q11 11 17 25.5t6 30.5v167q0 17-11.5 28.5T760-400q-17 0-28.5-11.5T720-440v-160H560q-17 0-28.5-11.5T520-640v-160H240v640h320q17 0 28.5 11.5T600-120q0 17-11.5 28.5T560-80H240Zm520-103v49q0 17-11.5 28.5T720-94q-17 0-28.5-11.5T680-134v-146q0-17 11.5-28.5T720-320h146q17 0 28.5 11.5T906-280q0 17-11.5 28.5T866-240h-50l90 90q11 11 11 27.5T906-94q-12 12-28.5 12T849-94l-89-89Zm-520 23v-640 640Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Info: ImageVector by lazy {
        ImageVector.Builder("Info", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-280q17 0 28.5-11.5T520-320v-160q0-17-11.5-28.5T480-520q-17 0-28.5 11.5T440-480v160q0 17 11.5 28.5T480-280Zm0-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val KeyboardArrowDown: ImageVector by lazy {
        ImageVector.Builder("KeyboardArrowDown", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-361q-8 0-15-2.5t-13-8.5L268-556q-11-11-11-28t11-28q11-11 28-11t28 11l156 156 156-156q11-11 28-11t28 11q11 11 11 28t-11 28L508-372q-6 6-13 8.5t-15 2.5Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val KeyboardArrowUp: ImageVector by lazy {
        ImageVector.Builder("KeyboardArrowUp", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-528 324-372q-11 11-28 11t-28-11q-11-11-11-28t11-28l184-184q12-12 28-12t28 12l184 184q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-528Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val MoreCircle: ImageVector by lazy {
        ImageVector.Builder("MoreCircle", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M240-400q-33 0-56.5-23.5T160-480q0-33 23.5-56.5T240-560q33 0 56.5 23.5T320-480q0 33-23.5 56.5T240-400Zm240 0q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm240 0q-33 0-56.5-23.5T640-480q0-33 23.5-56.5T720-560q33 0 56.5 23.5T800-480q0 33-23.5 56.5T720-400Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Ok: ImageVector by lazy {
        ImageVector.Builder("Ok", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m382-354 339-339q12-12 28-12t28 12q12 12 12 28.5T777-636L410-268q-12 12-28 12t-28-12L182-440q-12-12-11.5-28.5T183-497q12-12 28.5-12t28.5 12l142 143Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Pin: ImageVector by lazy {
        ImageVector.Builder("Pin", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M640-760v280l68 68q6 6 9 13.5t3 15.5v23q0 17-11.5 28.5T680-320H520v223q0 8-3 15.5T508-68l-14 14q-6 6-14 6t-14-6l-14-14q-6-6-9-13.5T440-97v-223H280q-17 0-28.5-11.5T240-360v-23q0-8 3-15.5t9-13.5l68-68v-280q-17 0-28.5-11.5T280-800q0-17 11.5-28.5T320-840h320q17 0 28.5 11.5T680-800q0 17-11.5 28.5T640-760ZM354-400h252l-46-46v-314H400v314l-46 46Zm126 0Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Refresh: ImageVector by lazy {
        ImageVector.Builder("Refresh", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-160q-134 0-227-93t-93-227q0-134 93-227t227-93q69 0 132 28.5T720-690v-70q0-17 11.5-28.5T760-800q17 0 28.5 11.5T800-760v200q0 17-11.5 28.5T760-520H560q-17 0-28.5-11.5T520-560q0-17 11.5-28.5T560-600h128q-32-56-87.5-88T480-720q-100 0-170 70t-70 170q0 100 70 170t170 70q68 0 124.5-34.5T692-367q8-14 22.5-19.5t29.5-.5q16 5 23 21t-1 30q-41 80-117 128t-169 48Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val RemoveCircleOutline: ImageVector by lazy {
        ImageVector.Builder("RemoveCircleOutline", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m480-424 116 116q11 11 28 11t28-11q11-11 11-28t-11-28L536-480l116-116q11-11 11-28t-11-28q-11-11-28-11t-28 11L480-536 364-652q-11-11-28-11t-28 11q-11 11-11 28t11 28l116 116-116 116q-11 11-11 28t11 28q11 11 28 11t28-11l116-116Zm0 344q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Search: ImageVector by lazy {
        ImageVector.Builder("Search", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M380-320q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l224 224q11 11 11 28t-11 28q-11 11-28 11t-28-11L532-372q-30 24-69 38t-83 14Zm0-80q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val SearchCleanup: ImageVector by lazy {
        ImageVector.Builder("SearchCleanup", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Settings: ImageVector by lazy {
        ImageVector.Builder("Settings", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M433-80q-27 0-46.5-18T363-142l-9-66q-13-5-24.5-12T307-235l-62 26q-25 11-50 2t-39-32l-47-82q-14-23-8-49t27-43l53-40q-1-7-1-13.5v-27q0-6.5 1-13.5l-53-40q-21-17-27-43t8-49l47-82q14-23 39-32t50 2l62 26q11-8 23-15t24-12l9-66q4-26 23.5-44t46.5-18h94q27 0 46.5 18t23.5 44l9 66q13 5 24.5 12t22.5 15l62-26q25-11 50-2t39 32l47 82q14 23 8 49t-27 43l-53 40q1 7 1 13.5v27q0 6.5-2 13.5l53 40q21 17 27 43t-8 49l-48 82q-14 23-39 32t-50-2l-60-26q-11 8-23 15t-24 12l-9 66q-4 26-23.5 44T527-80h-94Zm7-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Sort: ImageVector by lazy {
        ImageVector.Builder("Sort", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M160-240q-17 0-28.5-11.5T120-280q0-17 11.5-28.5T160-320h160q17 0 28.5 11.5T360-280q0 17-11.5 28.5T320-240H160Zm0-200q-17 0-28.5-11.5T120-480q0-17 11.5-28.5T160-520h400q17 0 28.5 11.5T600-480q0 17-11.5 28.5T560-440H160Zm0-200q-17 0-28.5-11.5T120-680q0-17 11.5-28.5T160-720h640q17 0 28.5 11.5T840-680q0 17-11.5 28.5T800-640H160Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Tune: ImageVector by lazy {
        ImageVector.Builder("Tune", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-120q-17 0-28.5-11.5T440-160v-160q0-17 11.5-28.5T480-360q17 0 28.5 11.5T520-320v40h280q17 0 28.5 11.5T840-240q0 17-11.5 28.5T800-200H520v40q0 17-11.5 28.5T480-120Zm-320-80q-17 0-28.5-11.5T120-240q0-17 11.5-28.5T160-280h160q17 0 28.5 11.5T360-240q0 17-11.5 28.5T320-200H160Zm160-160q-17 0-28.5-11.5T280-400v-40H160q-17 0-28.5-11.5T120-480q0-17 11.5-28.5T160-520h120v-40q0-17 11.5-28.5T320-600q17 0 28.5 11.5T360-560v160q0 17-11.5 28.5T320-360Zm160-80q-17 0-28.5-11.5T440-480q0-17 11.5-28.5T480-520h320q17 0 28.5 11.5T840-480q0 17-11.5 28.5T800-440H480Zm160-160q-17 0-28.5-11.5T600-640v-160q0-17 11.5-28.5T640-840q17 0 28.5 11.5T680-800v40h120q17 0 28.5 11.5T840-720q0 17-11.5 28.5T800-680H680v40q0 17-11.5 28.5T640-600Zm-480-80q-17 0-28.5-11.5T120-720q0-17 11.5-28.5T160-760h320q17 0 28.5 11.5T520-720q0 17-11.5 28.5T480-680H160Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Unpin: ImageVector by lazy {
        ImageVector.Builder("Unpin", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M560-760H400v87L290-783q-5-5-7.5-11t-2.5-12q0-13 9-23.5t24-10.5h327q17 0 28.5 11.5T680-800q0 16-14.5 22.5T640-760v240q0 17-11.5 28.5T600-480q-17 0-28.5-11.5T560-520v-240ZM440-80v-240H296q-25 0-40-17.5T241-377q0-11 4.5-22t14.5-21l60-60v-46L84-764q-11-11-11.5-27.5T84-820q11-11 28-11t28 11l679 679q12 12 11.5 28.5T818-84q-12 11-28 11.5T762-84L526-320h-6v240q0 17-11.5 28.5T480-40q-17 0-28.5-11.5T440-80Zm-86-320h92l-44-44-2-2-46 46Zm126-193Zm-78 149Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val UploadCloud: ImageVector by lazy {
        ImageVector.Builder("UploadCloud", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "m440-446-36 35q-11 11-27.5 11T348-412q-11-11-11-28t11-28l104-104q12-12 28-12t28 12l104 104q11 11 11.5 27.5T612-412q-11 11-27.5 11.5T556-411l-36-35v206h220q42 0 71-29t29-71q0-42-29-71t-71-29h-60v-80q0-83-58.5-141.5T480-720q-83 0-141.5 58.5T280-520h-20q-58 0-99 41t-41 99q0 58 41 99t99 41h60q17 0 28.5 11.5T360-200q0 17-11.5 28.5T320-160h-60q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H520q-33 0-56.5-23.5T440-240v-206Zm40 6Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }
    val Wifi: ImageVector by lazy {
        ImageVector.Builder("Wifi", 24.dp, 24.dp, 960.0f, 960.0f)
            .apply {
                addGroup(translationY = 960f)
                addPath(
                    pathData =
                        addPathNodes(
                            "M480-120q-42 0-71-29t-29-71q0-42 29-71t71-29q42 0 71 29t29 71q0 42-29 71t-71 29Zm0-440q75 0 142.5 24T745-470q20 15 20.5 39.5T748-388q-17 17-42 17.5T661-384q-38-26-84-41t-97-15q-51 0-97 15t-84 41q-20 14-45 13t-42-18q-17-18-17-42.5t20-39.5q55-42 122.5-65.5T480-560Zm0-240q125 0 235.5 41T914-643q20 17 21 42t-17 43q-17 17-42 17.5T831-556q-72-59-161.5-91.5T480-680q-100 0-189.5 32.5T129-556q-20 16-45 15.5T42-558q-18-18-17-43t21-42q88-75 198.5-116T480-800Z"
                        ),
                    fill = SolidColor(Color.White),
                )
                clearGroup()
            }
            .build()
    }

    val Power: ImageVector by lazy {
        ImageVector.Builder("Power", 24.dp, 24.dp, 960f, 960f)
            .apply {
                addPath(
                    addPathNodes(
                        "M440,480L440,160Q440,143 451.5,131.5Q463,120 480,120Q497,120 508.5,131.5Q520,143 520,160L520,480Q520,497 508.5,508.5Q497,520 480,520Q463,520 451.5,508.5Q440,497 440,480ZM480,840Q406,840 340.5,811.5Q275,783 226,734Q177,685 148.5,619.5Q120,554 120,480Q120,411 145.5,347.5Q171,284 217,234Q228,222 245,222Q262,222 274,234Q285,245 284,261.5Q283,278 272,291Q238,329 219,377.5Q200,426 200,480Q200,596 282,678Q364,760 480,760Q597,760 678.5,678Q760,596 760,480Q760,426 741,376.5Q722,327 687,289Q676,277 675.5,261Q675,245 686,234Q698,222 715,222Q732,222 743,234Q790,284 815,347.5Q840,411 840,480Q840,554 811.5,619.5Q783,685 734.5,734Q686,783 620.5,811.5Q555,840 480,840Z"
                    ),
                    fill = SolidColor(Color.White),
                )
            }
            .build()
    }
    val Stop: ImageVector by lazy {
        ImageVector.Builder("Stop", 24.dp, 24.dp, 960f, 960f)
            .apply {
                addPath(
                    addPathNodes(
                        "M240,640L240,320Q240,287 263.5,263.5Q287,240 320,240L640,240Q673,240 696.5,263.5Q720,287 720,320L720,640Q720,673 696.5,696.5Q673,720 640,720L320,720Q287,720 263.5,696.5Q240,673 240,640ZM320,640L640,640Q640,640 640,640Q640,640 640,640L640,320Q640,320 640,320Q640,320 640,320L320,320Q320,320 320,320Q320,320 320,320L320,640Q320,640 320,640Q320,640 320,640ZM480,480Q480,480 480,480Q480,480 480,480L480,480Q480,480 480,480Q480,480 480,480L480,480Q480,480 480,480Q480,480 480,480L480,480Q480,480 480,480Q480,480 480,480L480,480Z"
                    ),
                    fill = SolidColor(Color.White),
                )
            }
            .build()
    }
    val Restart: ImageVector by lazy {
        ImageVector.Builder("Restart", 24.dp, 24.dp, 960f, 960f)
            .apply {
                addPath(
                    addPathNodes(
                        "M393,828Q290,799 225,714.5Q160,630 160,520Q160,463 179,411.5Q198,360 233,317Q244,305 260,304.5Q276,304 289,317Q300,328 300.5,344Q301,360 290,374Q266,405 253,442Q240,479 240,520Q240,601 287.5,664.5Q335,728 410,751Q423,755 431.5,766Q440,777 440,790Q440,810 426,821.5Q412,833 393,828ZM567,828Q548,833 534,821Q520,809 520,789Q520,777 528.5,766Q537,755 550,751Q625,727 672.5,664Q720,601 720,520Q720,420 650,350Q580,280 480,280L477,280L493,296Q504,307 504,324Q504,341 493,352Q482,363 465,363Q448,363 437,352L353,268Q347,262 344.5,255Q342,248 342,240Q342,232 344.5,225Q347,218 353,212L437,128Q448,117 465,117Q482,117 493,128Q504,139 504,156Q504,173 493,184L477,200L480,200Q614,200 707,293Q800,386 800,520Q800,629 735,714Q670,799 567,828Z"
                    ),
                    fill = SolidColor(Color.White),
                )
            }
            .build()
    }
    val Clock: ImageVector by lazy {
        ImageVector.Builder("Clock", 24.dp, 24.dp, 960f, 960f)
            .apply {
                addPath(
                    addPathNodes(
                        "M520,464L520,320Q520,303 508.5,291.5Q497,280 480,280Q463,280 451.5,291.5Q440,303 440,320L440,479Q440,487 443,494.5Q446,502 452,508L584,640Q595,651 612,651Q629,651 640,640Q651,629 651,612Q651,595 640,584L520,464ZM480,880Q397,880 324,848.5Q251,817 197,763Q143,709 111.5,636Q80,563 80,480Q80,397 111.5,324Q143,251 197,197Q251,143 324,111.5Q397,80 480,80Q563,80 636,111.5Q709,143 763,197Q817,251 848.5,324Q880,397 880,480Q880,563 848.5,636Q817,709 763,763Q709,817 636,848.5Q563,880 480,880ZM480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480Q480,480 480,480ZM480,800Q613,800 706.5,706.5Q800,613 800,480Q800,347 706.5,253.5Q613,160 480,160Q347,160 253.5,253.5Q160,347 160,480Q160,613 253.5,706.5Q347,800 480,800Z"
                    ),
                    fill = SolidColor(Color.White),
                )
            }
            .build()
    }
    val SortName: ImageVector by lazy {
        ImageVector.Builder("SortName", 24.dp, 24.dp, 960f, 960f)
            .apply {
                addPath(
                    addPathNodes(
                        "M196,584L173,654Q169,665 159,672.5Q149,680 137,680Q117,680 104.5,663.5Q92,647 100,627L220,306Q225,294 235,287Q245,280 258,280L288,280Q301,280 311,287Q321,294 326,306L447,629Q454,648 442.5,664Q431,680 411,680Q399,680 389,672.5Q379,665 375,654L350,584L196,584ZM220,516L324,516L276,366L270,366L220,516ZM638,608L804,608Q819,608 829.5,618.5Q840,629 840,644Q840,659 829.5,669.5Q819,680 804,680L572,680Q562,680 555,673Q548,666 548,656L548,618Q548,611 550,604.5Q552,598 557,593L750,352L592,352Q577,352 566.5,341.5Q556,331 556,316Q556,301 566.5,290.5Q577,280 592,280L814,280Q824,280 831,287Q838,294 838,304L838,342Q838,349 836,355.5Q834,362 829,367L638,608ZM384,200Q377,200 374.5,194Q372,188 377,183L466,94Q472,88 480,88Q488,88 494,94L583,183Q588,188 585.5,194Q583,200 576,200L384,200ZM466,866L377,777Q372,772 374.5,766Q377,760 384,760L576,760Q583,760 585.5,766Q588,772 583,777L494,866Q488,872 480,872Q472,872 466,866Z"
                    ),
                    fill = SolidColor(Color.White),
                )
            }
            .build()
    }
}
