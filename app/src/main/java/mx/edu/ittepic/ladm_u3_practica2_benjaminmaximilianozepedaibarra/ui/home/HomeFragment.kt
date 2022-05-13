package mx.edu.ittepic.ladm_u3_practica2_benjaminmaximilianozepedaibarra.ui.home

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u3_practica2_benjaminmaximilianozepedaibarra.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val baseDatos = FirebaseFirestore.getInstance()
    private var id = "-1"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.bDescripcion.setOnClickListener {
            var busqueda = binding.tBusqueda.text.toString()
            consultarPDescripcion(busqueda)
        }

        binding.bDivision.setOnClickListener {
            var busqueda = binding.tBusqueda.text.toString()
            consultarPDivision(busqueda)
        }

        binding.bInsertar.setOnClickListener {
            val baseRemota = FirebaseFirestore.getInstance()
            val datos = hashMapOf(
                "descripcion" to binding.aDescripcion.text.toString(),
                "division" to binding.aDivision.text.toString(),
                "cantidad_empleados" to binding.aCEmpleados.text.toString().toInt()
            )
            baseRemota.collection("area")
                .add(datos)
                .addOnSuccessListener{
                    //SI SE PUDO!
                    Toast.makeText(requireContext(),
                        "Dato insertado exitosamente",
                        Toast.LENGTH_LONG)
                        .show()
                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(),
                        "No se ha insertado de forma correcta",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            binding.aDescripcion.setText("")
            binding.aDivision.setText("")
            binding.aCEmpleados.setText("")
        }

        binding.bEliminar.setOnClickListener {
            if (id.equals("-1")){
                Toast.makeText(requireContext(),
                    "Elije un dato del listView",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                baseDatos
                    .collection("area")
                    .document(id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Dato eliminado exitosamente",
                            Toast.LENGTH_SHORT)
                            .show()
                        binding.aDescripcion.setText("")
                        binding.aDivision.setText("")
                        binding.aCEmpleados.setText("")
                        id="-1"
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "No se ha eliminado de forma correcta",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        binding.bActualizar.setOnClickListener {
            var descripcion = binding.aDescripcion.text.toString()
            var division = binding.aDivision.text.toString()
            var nEmpleados = binding.aCEmpleados.text.toString().toInt()
            if (id.equals("-1")){
                Toast.makeText(requireContext(),
                    "Elije un dato del listView",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                baseDatos
                    .collection("area")
                    .document(id)
                    .update(
                        "cantidad_empleados",nEmpleados,
            "descripcion",descripcion,
                             "division",division
                    ).addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Dato actualizado exitosamente",
                            Toast.LENGTH_SHORT)
                            .show()
                        binding.aDescripcion.setText("")
                        binding.aDivision.setText("")
                        binding.aCEmpleados.setText("")
                        id="-1"
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "No se ha actualizado de forma correcta",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        binding.listaDatos.setOnItemClickListener{ adapterView, view, i, l ->
            var dato = adapterView.getItemAtPosition(i).toString()
            var datos = dato.split("\n")
            var splits0 = datos[0].split(":")
            var splits1 = datos[1].split(":")
            var splits2 = datos[2].split(":")
            var splits3 = datos[3].split(":")
            id = splits0[1]
            binding.aDescripcion.setText(splits1[1])
            binding.aDivision.setText(splits2[1])
            binding.aCEmpleados.setText(splits3[1])
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun consultarPDivision(divisionBuscar : String){
        baseDatos
            .collection("area")
            .whereEqualTo("division", divisionBuscar)
            .addSnapshotListener{ query, error->
                if(error!=null){
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                val arreglo = ArrayList<String>()
                for (documento in query!!){
                    var cadena =
                            "id:${documento.id}\n"+
                            "descripcion:${documento.getString("descripcion")}\n"+
                            "division:${documento.getString("division")}\n"+
                            "cantidad_empleados:${documento.getLong("cantidad_empleados")}"
                    arreglo.add(cadena)
                }
                if (arreglo.size == 0){
                    Toast.makeText(requireContext(),
                        "No se encontraron datos",
                        Toast.LENGTH_SHORT)
                        .show()
                    binding.listaDatos.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                } else {
                    binding.listaDatos.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                }
            }
    }

    fun consultarPDescripcion(descripcionBuscar : String){
        baseDatos
            .collection("area")
            .whereEqualTo("descripcion", descripcionBuscar)
            .addSnapshotListener{ query, error->
                if(error!=null){
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                val arreglo = ArrayList<String>()
                for (documento in query!!){
                    var cadena =
                            "descripcion: ${documento.getString("descripcion")}\n"+
                            "division: ${documento.getString("division")}\n"+
                            "cantidad_empleados: ${documento.getLong("cantidad_empleados")}"
                    arreglo.add(cadena)
                }
                if (arreglo.size == 0){
                    Toast.makeText(requireContext(),
                        "No se encontraron datos",
                        Toast.LENGTH_SHORT)
                        .show()
                    binding.listaDatos.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                } else {
                    binding.listaDatos.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                }
            }
    }
}