package io.bluetrace.opentrace.infraestructura.repositorioImpl

import io.bluetrace.opentrace.domain.repository.IRepositoryIntroItems
import io.bluetrace.opentrace.ui.IntroItemModel
import javax.inject.Inject

class IntroItemsRepositorioImpl @Inject constructor(): IRepositoryIntroItems {

    override fun getIntroItems() = generateIntroItems()
    private fun generateIntroItems():ArrayList<IntroItemModel>{

        var introModelList = ArrayList<IntroItemModel>();

        val propositoApp = IntroItemModel("Proposito de la aplicacion",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.  ",
            "")

        val para_usar = IntroItemModel("Para usar",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.  ",
            "")

        val corre_la_voz = IntroItemModel("Corre la voz en tu trabajo",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.  ",
            "")

        val entre_todos = IntroItemModel("Entre todos nos protegemos",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.  ",
            "")

        introModelList.add(propositoApp)
        introModelList.add(para_usar)
        introModelList.add(corre_la_voz)
        introModelList.add(entre_todos)

        return introModelList
    }

}