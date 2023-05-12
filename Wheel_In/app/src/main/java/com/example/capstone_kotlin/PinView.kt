package com.example.capstone_kotlin

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class PinView @JvmOverloads constructor(context: Context?, attr: AttributeSet? = null) :
    SubsamplingScaleImageView(context, attr) {

    data class Pin(var id: String, var point: PointF, var fixed: Int, var imageId: Int, var width: Float, var height: Float, var text: String)
    data class Line(var id: String, var point: PointF, var color: Int)

    private val paint = Paint()
    private val vPin = PointF()
    private var sPin: PointF? = null
    private var iPin: Bitmap? = null
    private var pinArray = ArrayList<Pin>()
    private var lineArray = ArrayList<Line>()
    var w: Float? = null
    var h: Float? = null


    /**
     * 지도(기본 이미지)위에 기본 Pin을 추가하고 나머지는 초기화합니다.
     * @param nPin Pin 이 표시될 좌표값입니다.
     */
    fun setPin(sPin: PointF?) {
        pinArray = arrayListOf()
        //pinArray.add(Pin("0", sPin!!, 0, R.drawable.pushpin_blue, ""))
        invalidate()
        this.sPin = sPin
        //initialise()
        invalidate()
    }

    /**
     * 지도(기본 이미지)위에 표시할 Pin을 추가합니다.
     * @param point Pin 이 표시될 좌표값입니다.
     * @param fix Pin 이미지가 지도와 함께 축소, 확대될지 결정합니다. 0:적용 1:적용안함
     * @param imageID Pin 이미지의 ID 값입니다. (ex: R.drawable.image_name)
     */
    fun addPin(point: PointF, fix: Int = 0, imageID: Int = R.drawable.pushpin_blue, id: String = "0", width:Float = 2.0f, height: Float = 1.0f, text: String = "")
    {
        pinArray.add(Pin(id, point, fix, imageID, width, height, text))
        invalidate()
    }
    // 설정된 자료형 기준 순서 위는 기존 설계 + 제작하면서 추가된 순서 - 기존 코드 변경을 막기 위함.
    fun addPin(id: String = "0", nPin: PointF, fix: Int = 0, imageID: Int = R.drawable.pushpin_blue, width:Float = 2.0f, height: Float = 1.0f, text: String = "")
    {
        pinArray.add(Pin(id, nPin, fix, imageID, width, height, text))
        invalidate()
    }
    // 설정된 자료형 기준 순서 + width , height 의 자료형이 Int
    fun addPin(id: String = "0", nPin: PointF, fix: Int = 0, imageID: Int = R.drawable.pushpin_blue, width:Int = 2, height: Int = 1, text: String = "")
    {
        pinArray.add(Pin(id, nPin, fix, imageID, width.toFloat(), height.toFloat(), text))
        invalidate()
    }

    /**
     * 지도(기본 이미지)위에 표시할 Point를 추가합니다. 이 Point들은 2개 이상일 경우 연결되어 선을 표시합니다.
     * @param point PointF 형식의 좌표입니다.
     * @param color 표현될 선의 색깔입니다. 1번 좌표, 2번 좌표 를 추가했을때 1번 좌표와 함께 입력된 색깔이 선의 색깔이 됩니다.
     */
    fun addLine(point: PointF, color: Int, id: String = "0")
    {
        lineArray.add(Line(id, point, color))
        invalidate()
    }

    /**
     * 지도(기본 이미지)위에 표시된 Pin을 모두 제거합니다.
     */
    fun clearPin()
    {
        //pinArray = arrayListOf()
        lineArray = arrayListOf()
        invalidate()
    }

    private fun initialise() {invalidate()}

    /**
     * 저장된 좌표, 라인들을 지도 위에 표시합니다.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) {
            return
        }
        paint.isAntiAlias = true

        // Line 파트
        // Line 관련 정보 설정
        val myPaint = Paint()
        myPaint.strokeWidth = 10f
        myPaint.style = Paint.Style.FILL
        myPaint.isAntiAlias = true
        myPaint.strokeCap = Paint.Cap.ROUND
        for (i in 0..lineArray.size)
        {
            if (i >= lineArray.size-1)
            {
                break
            }
            var line = lineArray.get(i)
            myPaint.color = line.color
            var pointTmp1 = PointF()
            var pointTmp2 = PointF()
            sourceToViewCoord(line.point, pointTmp1)
            sourceToViewCoord(lineArray.get(i+1).point, pointTmp2)
            canvas.drawLine(pointTmp1.x, pointTmp1.y,pointTmp2.x, pointTmp2.y, myPaint)
        }

        // Marker 파트
        var s = scale
        val density = resources.displayMetrics.densityDpi.toFloat()
        myPaint.textAlign = Paint.Align.CENTER
        for(i in pinArray)
        {
            var pin = i.point
            var fix = i.fixed
            var imageId = i.imageId
            sourceToViewCoord(pin, vPin)
            var image = BitmapFactory.decodeResource(this.resources, imageId)
            w = density / 420f * image!!.getWidth()
            h = density / 420f * image!!.getHeight()

            if(fix == 1) // 확대 축소에 따라 크기가 변하지 않음
            {
                image = Bitmap.createScaledBitmap(image!!, (w!!).toInt(), (h!!).toInt(), true)
                myPaint.textSize = 100.0f
            }
            else // 확대 축소에 따라 크기가 변함
            {
                image= Bitmap.createScaledBitmap(image!!, (w!!*s).toInt(), (h!!*s).toInt(), true)
                myPaint.textSize = 100.0f * s
            }
            val vX = vPin.x - image!!.width / i.width //(/2가 없는 경우 해당 좌표기준 좌측 위로 이미지가 생성됨)
            val vY = vPin.y - image!!.height / i.height
            canvas.drawBitmap(image!!, vX, vY, paint)
            myPaint.setColor(Color.RED)
            canvas.drawText(i.text, vPin.x, vPin.y+myPaint.textSize, myPaint)
        }
    }

    init {initialise()}
}