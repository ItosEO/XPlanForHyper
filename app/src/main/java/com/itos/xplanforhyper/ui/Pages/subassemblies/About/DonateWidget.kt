import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.itos.xplanforhyper.R
import com.itos.xplanforhyper.datatype.OriginCardItem
import com.itos.xplanforhyper.utils.OUI

@Composable
fun DonateWidget() {
    LocalContext.current

    val items = listOf(
        OriginCardItem(
            icon = ImageVector.vectorResource(R.drawable.ic_alipay),
            label = "支付宝",
            onClick = {
                OUI.showImageDialog("zfb.jpg")
            }
        ),

        )
    ItemsCardWidget(
        title = {
            Text(text = "捐赠")
        },
        items = items,
        showItemIcon = true
    )
}
