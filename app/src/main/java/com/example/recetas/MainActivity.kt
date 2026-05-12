package com.example.recetas
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get
import android.widget.LinearLayout
import android.view.View

// Agrega la variable aquí abajo
private lateinit var layoutReceta: LinearLayout
class MainActivity : AppCompatActivity() {

    private lateinit var btnCargarReceta: Button
    private lateinit var tvNombrePlato: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var imgPlato: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCargarReceta = findViewById(R.id.btnCargarReceta)
        tvNombrePlato = findViewById(R.id.tvNombrePlato)
        tvIngredientes = findViewById(R.id.tvIngredientes)
        tvInstrucciones = findViewById(R.id.tvInstrucciones)
        imgPlato = findViewById(R.id.imgPlato)
        layoutReceta = findViewById(R.id.layoutReceta)
        btnCargarReceta.setOnClickListener {
            obtenerRecetaAleatoria()
        }
    }

    private fun obtenerRecetaAleatoria() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getRandomRecipe()
                if (response.isSuccessful && response.body() != null) {
                    val recetaJson = response.body()!!.meals[0]
                    withContext(Dispatchers.Main) {
                        mostrarReceta(recetaJson)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarReceta(receta: JsonObject) {
        val nombre = receta.get("strMeal").asString
        val instrucciones = receta.get("strInstructions").asString
        val urlImagen = receta.get("strMealThumb").asString
        layoutReceta.visibility = View.VISIBLE
        tvNombrePlato.text = nombre
        tvInstrucciones.text = instrucciones
        Glide.with(this)
            .load(urlImagen)
            .into(imgPlato)
        val listaIngredientes = StringBuilder()
        for (i in 1..20) {
            val ingredienteElement = receta.get("strIngredient$i")
            val medidaElement = receta.get("strMeasure$i")
            val ingrediente = if (ingredienteElement != null && !ingredienteElement.isJsonNull) ingredienteElement.asString else ""
            val medida = if (medidaElement != null && !medidaElement.isJsonNull) medidaElement.asString else ""
            if (ingrediente.isNotBlank()) {
                listaIngredientes.append("• $medida $ingrediente\n")
            }
        }
        tvIngredientes.text = listaIngredientes.toString()
    }
}