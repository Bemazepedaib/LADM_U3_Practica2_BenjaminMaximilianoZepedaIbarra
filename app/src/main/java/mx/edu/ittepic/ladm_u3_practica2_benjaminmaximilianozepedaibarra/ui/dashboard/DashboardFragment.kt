package mx.edu.ittepic.ladm_u3_practica2_benjaminmaximilianozepedaibarra.ui.dashboard

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u3_practica2_benjaminmaximilianozepedaibarra.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var idArea="-1"
    private var idSub="-1"
    private val baseDatos = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        consultar()

        binding.bInsertar.setOnClickListener {
            if (idArea.equals("-1")){
                Toast.makeText(requireContext(), "Seleccione un Area primero", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                var idEdificio = binding.aIEdificio.text.toString().toInt()
                var piso = binding.aPiso.text.toString().toInt()
                val datos = hashMapOf(
                    "idedificio" to idEdificio,
                    "piso" to piso,
                    "idarea" to idArea
                )
                baseDatos.collection("subdepartamento")
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
                binding.aIEdificio.setText("")
                binding.aPiso.setText("")
                idArea="-1"
                Toast.makeText(requireContext(), "Se ha agregado correctamente", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.bActualizar.setOnClickListener{
            if (idSub.equals("-1")){
                Toast.makeText(requireContext(), "Seleccione un SubDepartamento primero", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                var idEdificio = binding.aIEdificio.text.toString().toInt()
                var piso = binding.aPiso.text.toString().toInt()
                baseDatos
                    .collection("subdepartamento")
                    .document(idSub)
                    .update(
                        "idarea", idArea,
                        "idedificio",idEdificio,
                        "piso",piso
                    ).addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Dato actualizado exitosamente",
                            Toast.LENGTH_SHORT)
                            .show()
                        binding.aIEdificio.setText("")
                        binding.aPiso.setText("")
                        idSub="-1"
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "No se ha actualizado de forma correcta",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                mostrarDatos(idArea)
            }
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
                    .collection("subdepartamento")
                    .document(idSub)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Dato eliminado exitosamente",
                            Toast.LENGTH_SHORT)
                            .show()
                        binding.aIEdificio.setText("")
                        binding.aPiso.setText("")
                        idSub="-1"
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "No se ha eliminado de forma correcta",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        binding.listaDatosArea.setOnItemClickListener{ adapterView, view, i, l ->
            var dato = adapterView.getItemAtPosition(i).toString()
            var datos = dato.split("\n")
            var splits0 = datos[0].split(":")
            idArea = splits0[1]
            mostrarDatos(idArea)
        }

        binding.listaDatosEdificio.setOnItemClickListener { adapterView, view, i, l ->
            var dato = adapterView.getItemAtPosition(i).toString()
            var datos = dato.split("\n")
            var splits0 = datos[0].split(":")
            var splits1 = datos[1].split(":")
            var splits2 = datos[2].split(":")
            var splits3 = datos[3].split(":")
            idSub = splits0[1]
            binding.aIEdificio.setText(splits1[1])
            binding.aPiso.setText(splits2[1])
            idArea = splits3[1]
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun mostrarDatos(idArea : String){
        baseDatos
            .collection("subdepartamento")
            .whereEqualTo("idarea", idArea)
            .addSnapshotListener { query, error ->
                if (error != null) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                val arreglo = ArrayList<String>()
                for (documento in query!!) {
                    var cadena =
                        "idSubdepartamento:${documento.id}\n"+
                        "idedificio:${documento.getLong("idedificio")}\n" +
                        "piso:${documento.getLong("piso")}\n"+
                        "idArea:${documento.getString("idarea")}\n"
                    arreglo.add(cadena)
                }
                if (arreglo.size == 0){
                    Toast.makeText(requireContext(),
                        "No se encontraron datos",
                        Toast.LENGTH_SHORT)
                        .show()
                    binding.listaDatosEdificio.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                } else {
                    binding.listaDatosEdificio.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                }
            }
    }

    fun consultar(){
        baseDatos
            .collection("area")
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
                    binding.listaDatosArea.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                } else {
                    binding.listaDatosArea.adapter = ArrayAdapter<String>(requireActivity(),
                        R.layout.simple_list_item_1, arreglo)
                }
            }
    }
}