package io.bluetrace.opentrace.infraestructura.repositorioImpl

import io.bluetrace.opentrace.domain.repository.IRepositoryIntroItems
import io.bluetrace.opentrace.ui.IntroItemModel
import javax.inject.Inject

class IntroItemsRepositorioImpl @Inject constructor(): IRepositoryIntroItems {

    override fun getIntroItems() = generateIntroItems()
    private fun generateIntroItems():ArrayList<IntroItemModel>{

        var introModelList = ArrayList<IntroItemModel>();

        val propositoApp = IntroItemModel("Proposito de la aplicacion",
            "SURA te acompaña a ti y a tus compañeros, entre todos nos cuidamos.",
            "proposito"
            )

        val para_usar = IntroItemModel("Para usar",
            "Debes tener el celular con el bluetooth prendido.",
            "usar_la_app")

        val corre_la_voz = IntroItemModel("Corre la voz en tu trabajo",
            "Cuéntale a tus compañeros que descarguen la App, porque juntos hacemos más.",
            "corre_la_voz")

        val entre_todos = IntroItemModel("Entre todos nos protegemos",
            "En caso de adquirir el COVID-19, podremos con los datos de tu bluetooth hacer un cerco epidemiológico.  ",
            "entre_todos")

        introModelList.add(propositoApp)
        introModelList.add(para_usar)
        introModelList.add(corre_la_voz)
        introModelList.add(entre_todos)

        return introModelList
    }

}