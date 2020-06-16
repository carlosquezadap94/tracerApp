package io.bluetrace.opentrace.domain.servicios

import io.bluetrace.opentrace.domain.repository.IRepositoryIntroItems
import io.bluetrace.opentrace.infraestructura.repositorioImpl.IntroItemsRepositorioImpl
import javax.inject.Inject

open class ServicioIntroItems @Inject constructor(iRepositoryIntroItems: IntroItemsRepositorioImpl) :
    IServiceIntroItems {

    private var iRepositoryIntroItems: IRepositoryIntroItems

    init {
        this.iRepositoryIntroItems = iRepositoryIntroItems
    }

    override fun getIntroItems() = iRepositoryIntroItems.getIntroItems()
}