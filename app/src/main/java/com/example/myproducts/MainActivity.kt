package com.example.myproducts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import com.example.myproducts.VolleySingleton
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    val IP = "http://192.168.56.1" // Dirección IP del servidor web que almacena los servicios web
    var bandera : Boolean = false
    var idProd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Este boton elimina los productos de la base de datos local y ejecuta el servicio web getProductos.php
    //Que consulta todos los producto del servidor web, para insertarlos en la base de datos local
    fun getAllProductos(view: View) {
        val wsURL = IP + "/wsAndroid/getProductos.php"
        val admin = AdminBD(this)
        admin.Ejecuta("DELETE FROM Producto")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,wsURL,null,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                val sensadoJson = response.getJSONArray("productos")
                for (i in 0 until sensadoJson.length()){
                    // Los nombres del getString son como los arroja el servicio web
                    val idprod = sensadoJson.getJSONObject(i).getString("idProd")
                    val nomprod = sensadoJson.getJSONObject(i).getString("nomProd")
                    val existencia = sensadoJson.getJSONObject(i).getString("existencia")
                    val precio = sensadoJson.getJSONObject(i).getString("precio")

                    val sentencia = "Insert into Producto(idProd,nomProd,Existencia,Precio) values (${idprod}, '${nomprod}',${existencia}, ${precio})"
                    val res = admin.Ejecuta(sentencia)
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error getAllProductos: " + error.message.toString() , Toast.LENGTH_LONG).show();
                Log.d("Zazueta",error.message.toString() )
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }


    fun btnBuscar(view:View){

        if (etIdProd.text.toString().isEmpty()){
            etIdProd.setError("Falta ingresar clave del producto")
            Toast.makeText(this, "Falta información del id", Toast.LENGTH_SHORT).show();
            etIdProd.requestFocus()
        }
        else{
            val wsURL = IP + "/wsAndroid/getProducto.php"
            val id= etIdProd.text.toString()
            var jsonEntrada = JSONObject()
            jsonEntrada.put("idProd",id)

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,wsURL,jsonEntrada,
                Response.Listener { response ->
                    val succ = response["success"]
                    val msg = response["message"]
                    if (succ == 200){
                        val sensadoJson = response.getJSONArray("producto")
                            // Los nombres del getString son como los arroja el servicio web
                            val idprod = sensadoJson.getJSONObject(0).getString("idProd")
                            val nomprod = sensadoJson.getJSONObject(0).getString("nomProd")
                            val existencia = sensadoJson.getJSONObject(0).getString("existencia")
                            val precio = sensadoJson.getJSONObject(0).getString("precio")

                            etIdProd.setText(idprod)
                            etNomProd.setText(nomprod)
                            etExistencia.setText(existencia)
                            etPrecio.setText(precio)

                    }
                    Toast.makeText(this, "Producto encontrado", Toast.LENGTH_SHORT).show();
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error getAllProductos: " + error.message.toString() , Toast.LENGTH_LONG).show();
                    Log.d("Zazueta",error.message.toString() )
                }
            )
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

        }

            //
    }


    // Esta consulta se hace en la base de datos local, partiendo de que la información ya se cargo
    // ejecutando el boton de Carga Productos del servidor web
    fun Consultar(view: View) {
        if (etIdProd.text.toString().isEmpty()){
            etIdProd.setError("Falta ingresar clave del producto")
            Toast.makeText(this, "Falta información del id", Toast.LENGTH_SHORT).show();
            etIdProd.requestFocus()
        }
        else{
            val admin = AdminBD(this)
            val id:String = etIdProd.text.toString()
            //                                       0     1         2          3
            val cur = admin.Consulta("select idProd,nomProd,Existencia,Precio from Producto where idProd=$id")
            if (cur!!.moveToFirst()){
                etNomProd.setText(cur.getString(1))
                etExistencia.setText(cur.getString(2))
                etPrecio.setText(cur.getString(3))
            }
            else
            {
                Toast.makeText(this, "No existe la Clave del Producto", Toast.LENGTH_LONG).show();
                etIdProd.requestFocus()
            }
        }
    }

    fun Actualiza(view: View) {
        if (etIdProd.text.toString().isEmpty() ||
            etNomProd.text.toString().isEmpty()||
            etExistencia.text.toString().isEmpty()||
            etPrecio.text.toString().isEmpty()){
            etIdProd.setError("Falta información de Ingresar")
            Toast.makeText(this, "Falta información de Ingresar",
                Toast.LENGTH_LONG).show();
            etIdProd.requestFocus()
        }
        else
        {
            val id= etIdProd.text.toString()
            val nom = etNomProd.text.toString()
            val existencia = etExistencia.text.toString()
            val precio = etPrecio.text.toString()
            var jsonEntrada = JSONObject()
            jsonEntrada.put("idProd",id)
            jsonEntrada.put("nomProd", nom)
            jsonEntrada.put("existencia", existencia)
            jsonEntrada.put("precio",precio)
            sendRequest(IP + "/wsAndroid/updateProducto.php",jsonEntrada)
        }
    }

    fun Limpiar(view: View) {
        etIdProd.setText("")
        etNomProd.setText("")
        etExistencia.setText("0")
        etPrecio.setText("0")
        etIdProd.requestFocus()
    }

    fun Agregar(view: View) {
        if (etNomProd.text.toString().isEmpty()||
            etExistencia.text.toString().isEmpty()||
            etPrecio.text.toString().isEmpty()){
            etIdProd.setError("Falta información de Ingresar")
            Toast.makeText(this, "Falta información de Ingresar",
                Toast.LENGTH_LONG).show();
            etIdProd.requestFocus()
        }
        else
        {
            val nom = etNomProd.text.toString()
            val existencia = etExistencia.text.toString()
            val precio = etPrecio.text.toString()
            var jsonEntrada = JSONObject()
            jsonEntrada.put("nomProd", etNomProd.text.toString())
            jsonEntrada.put("existencia", etExistencia.text.toString())
            jsonEntrada.put("precio",etPrecio.text.toString())
            sendRequest(IP + "/wsAndroid/insertProducto.php",jsonEntrada)
        }
    }

    fun Borrar(view: View) {
        if (etIdProd.text.toString().isEmpty()){
            etIdProd.setError("Falta información de Ingresar")
            Toast.makeText(this, "Falta información de Ingresar",
                Toast.LENGTH_LONG).show();
            etIdProd.requestFocus()
        }
        else
        {
            val id= etIdProd.text.toString()
            var jsonEntrada = JSONObject()
            jsonEntrada.put("idProd",id)

            sendRequest(IP + "/wsAndroid/deleteProducto.php",jsonEntrada)
        }
    }

    //Rutina para mandar ejecutar un web service de tipo Insert, Update o Delete
    fun sendRequest( wsURL: String, jsonEnt: JSONObject){
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, wsURL,jsonEnt,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                if (succ == 200){
                    etIdProd.setText("")
                    etNomProd.setText("")
                    etExistencia.setText("0")
                    etPrecio.setText("0")
                    etIdProd.requestFocus()
                    Toast.makeText(this, "Success:${succ}  Message:${msg} Se Inserto Producto en el Servidor Web", Toast.LENGTH_SHORT).show();
                }
            },
            Response.ErrorListener{error ->
                Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show();
                Log.d("ERROR","${error.message}");
                Toast.makeText(this, "Error de capa 8 checa URL", Toast.LENGTH_SHORT).show();
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}
