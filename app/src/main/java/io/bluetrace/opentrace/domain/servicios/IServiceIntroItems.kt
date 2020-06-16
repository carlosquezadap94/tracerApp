package io.bluetrace.opentrace.domain.servicios

import io.bluetrace.opentrace.ui.IntroItemModel

interface IServiceIntroItems {
    fun getIntroItems(): ArrayList<IntroItemModel>
}