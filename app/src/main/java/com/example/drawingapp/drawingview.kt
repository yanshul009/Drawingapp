package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class drawingview(context: Context, attrs:AttributeSet): View(context,attrs) {
    private var mdrawpath:CustomPath?=null
    private var mcanvasbitmap:Bitmap?=null
    private var mdrawpaint: Paint?=null
    private var mcanvaspaint:Paint?=null
    private var mbrusthsize:Float=0.toFloat()
    private var color= Color.BLACK
    private var canvas:Canvas?=null
    private val mpath=ArrayList<CustomPath>()
    private val mundopath=ArrayList<CustomPath>()

    init {
        setUpdrawing()
    }

    private fun setUpdrawing() {
        mdrawpaint=Paint()
        mdrawpath=CustomPath(color,mbrusthsize)
        mdrawpaint?.color=color
        mdrawpaint?.style=Paint.Style.STROKE
        mdrawpaint?.strokeJoin=Paint.Join.ROUND
        mcanvaspaint=Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mcanvasbitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mcanvasbitmap!!)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(mcanvasbitmap!=null)
        {
            canvas.drawBitmap(mcanvasbitmap!!,0f,0f,mcanvaspaint)
        }
        for(p in mpath)
        {
            mdrawpaint?.strokeWidth=p.brushthickness
            mdrawpaint?.color=p.color
            canvas.drawPath(p,mdrawpaint!!)
        }

        if(!mdrawpath!!.isEmpty)
        {
            mdrawpaint!!.strokeWidth=mdrawpath!!.brushthickness
            mdrawpaint!!.color=mdrawpath!!.color
            canvas.drawPath(mdrawpath!!,mdrawpaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchx=event.x
        val touchy=event.y
        when(event.action)
        {
            MotionEvent.ACTION_DOWN->{
                mdrawpath!!.color=color
                mdrawpath!!.brushthickness=mbrusthsize
                mdrawpath!!.reset()
                mdrawpath!!.moveTo(touchx,touchy)
            }
            MotionEvent.ACTION_MOVE->{
                mdrawpath!!.lineTo(touchx,touchy)
            }
            MotionEvent.ACTION_UP->{
                mpath.add(mdrawpath!!)
                mdrawpath=CustomPath(color,mbrusthsize)
            }
            else -> return false
        }

        invalidate()
        return true


    }

    fun setsizeforbrusth(newsize:Float)
    {
        mbrusthsize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newsize,resources.displayMetrics)
        mdrawpaint?.strokeWidth=mbrusthsize
    }

    fun setColor(newColor:String)
    {
        color=Color.parseColor(newColor)
        mdrawpaint?.color=color
    }

    fun onclickundo()
    {
        if(mpath.size>0)
        {
            mundopath.add(mpath.removeAt(mpath.size-1))
            invalidate()
        }
    }

    fun onclickredo()
    {
        if(mundopath.size>0)
        {
            mpath.add(mundopath.removeAt(mundopath.size-1))
            invalidate()
        }
    }

    internal inner class CustomPath(var color:Int,var brushthickness:Float): Path()

}