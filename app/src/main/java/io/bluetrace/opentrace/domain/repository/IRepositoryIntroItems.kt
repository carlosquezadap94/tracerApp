package io.bluetrace.opentrace.domain.repository

import io.bluetrace.opentrace.ui.IntroItemModel

interface IRepositoryIntroItems {
    fun getIntroItems(): ArrayList<IntroItemModel>
}