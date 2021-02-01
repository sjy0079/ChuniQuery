package org.bbs.chuniquery.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View

/**
 * 镂空某些view显示区域的mask drawable, 可以用来做一些遮罩
 *
 * @author BBS
 * @since  2019-09-02
 */
class HollowMaskDrawable(private val maskColor: Int, private val maskView: View, private vararg val hollowViews: View) : Drawable() {
    /**
     * 画笔, 用来绘制bitmap
     */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        maskView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    @Suppress("DEPRECATION")
    override fun draw(canvas: Canvas) {
        canvas.drawColor(maskColor)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        val locationMask = IntArray(2)
        maskView.getLocationInWindow(locationMask)

        for (view in hollowViews) {
            view.buildDrawingCache()
            val icon = view.drawingCache

            // 使用mask view和hollow view的相对坐标定位
            val locationBitmap = IntArray(2)
            view.getLocationInWindow(locationBitmap)

            canvas.drawBitmap(icon,
                    locationBitmap[0].toFloat() - locationMask[0].toFloat(),
                    locationBitmap[1].toFloat() - locationMask[1].toFloat(), paint)
        }
        paint.xfermode = null
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}