package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
   private var drawingview:drawingview?=null

    private var mimagecurrentbtn:ImageButton?=null

    val openGallerylauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== RESULT_OK && result.data!=null)
        {
            val imageback:ImageView=findViewById(R.id.background)
            imageback.setImageURI(result.data?.data)
        }
    }

    val requestPermission:ActivityResultLauncher<Array<String>> =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions-> permissions.entries.forEach{
            val perMissionName=it.key
            val isGranted=it.value
            if(isGranted)
            {
                Toast.makeText(this,"permission granted",Toast.LENGTH_LONG).show()
                val pickIntent=Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGallerylauncher.launch(pickIntent)
            }
        else
            {
                if(perMissionName==Manifest.permission.READ_EXTERNAL_STORAGE)
                {
                    Toast.makeText(this,"YOU DENIED THE PERMISSION",Toast.LENGTH_LONG).show()
                }
            }
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingview=findViewById(R.id.drawing_view)
        drawingview?.setsizeforbrusth(20.toFloat())
        val brush:ImageButton=findViewById(R.id.brush)
        val lineralayoutpaint=findViewById<LinearLayout>(R.id.paint_color)
        mimagecurrentbtn=lineralayoutpaint[1] as ImageButton
        mimagecurrentbtn?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        brush.setOnClickListener{
        showdiaglogbrushsize()
        }
        val ibGallery: ImageButton = findViewById(R.id.gallery)
        //TODO(Step 10 : Adding an click event to image button for selecting the image from gallery.)

        ibGallery.setOnClickListener {
            requestStoragePermission()
        }
        val icundo:ImageView=findViewById(R.id.undo)
        icundo.setOnClickListener {
            drawingview?.onclickundo()
        }

        val save:ImageButton=findViewById(R.id.save)
        save.setOnClickListener{
            if(readstorageallowed())
            {
                lifecycleScope.launch{
                        val fldrawingview:FrameLayout=findViewById(R.id.drawing_view_container)
                        savebitmapfile(getbitmap(fldrawingview))
                }
            }
        }

        val redo:ImageButton=findViewById(R.id.redo)
        redo.setOnClickListener {
            drawingview?.onclickredo()
        }


    }

    private fun getbitmap(view: View): Bitmap {

        val returnedbitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(returnedbitmap)
        val bgdrawable=view.background
        if(bgdrawable!=null)
        {
            bgdrawable.draw(canvas)
        }
        else
        {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedbitmap
    }

    private suspend fun savebitmapfile(bitmap:Bitmap?):String{
        var result=""
        withContext(Dispatchers.IO)
        {
            if(bitmap!=null)
            {
                try{
                    val bytes=ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f= File(externalCacheDir?.absoluteFile.toString()+File.separator+"KIDdrawingapp_"+System.currentTimeMillis()/100+".jpg")
                    val fo=FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result=f.absolutePath
                    runOnUiThread{
                        if(!result.isEmpty())
                        {
                            Toast.makeText(this@MainActivity,"FILE saved successfully: $result",Toast.LENGTH_LONG).show()
                            shareImage(result)
                        }
                        else
                        {
                            Toast.makeText(this@MainActivity,"sOMETHING WENT WRONG",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e:Exception)
                {
                    result=""
                    e.printStackTrace()
                }
            }
        }

        return result

    }

    private fun readstorageallowed(): Boolean {
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }

    private fun showdiaglogbrushsize() {
        val brushdialog=Dialog(this)
        brushdialog.setContentView(R.layout.brushsizelayout)
        brushdialog.setTitle("Brush Size")
        val smallbtn:ImageButton=brushdialog.findViewById(R.id.small_brush)
        smallbtn.setOnClickListener {
            drawingview?.setsizeforbrusth(10.toFloat())
            brushdialog.dismiss()
        }
        val mediumbtn:ImageButton=brushdialog.findViewById(R.id.medium_brush)
        mediumbtn.setOnClickListener {
            drawingview?.setsizeforbrusth(20.toFloat())
            brushdialog.dismiss()
        }

        val largebtn:ImageButton=brushdialog.findViewById(R.id.large_brush)
        largebtn.setOnClickListener {
            drawingview?.setsizeforbrusth(30.toFloat())
            brushdialog.dismiss()
        }
        brushdialog.show()
    }

    fun paintclicked(view: View)
    {
        if(view!==mimagecurrentbtn)
        {
            val imagebtn=view as ImageButton
            val colortag=imagebtn.tag.toString()
            drawingview?.setColor(colortag)
            imagebtn.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_pressed))
            mimagecurrentbtn?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))
        }

        mimagecurrentbtn=view as ImageButton
    }

    private fun requestStoragePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            showRationaleDialog("Drawing App","The app needs your permission to access storage")
        }
        else
        {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }

    }

    private fun shareImage(result:String)
    {
        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null)
        {
            path,uri->
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type="image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }

    private fun showRationaleDialog(title:String,message:String)
    {
        val builer:AlertDialog.Builder=AlertDialog.Builder(this)
        builer.setTitle(title).setMessage(message).setPositiveButton("Cancel"){
            dialog,_->dialog.dismiss()
        }
        builer.create().show()
    }

}