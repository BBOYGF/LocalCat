package com.felinetech.localcat.pojo

import androidx.compose.runtime.MutableState
import com.felinetech.localcat.enums.PayTypes
import org.jetbrains.compose.resources.DrawableResource

/**
 * 支付类型
 */
data class PayItem(var type: PayTypes, var title: String, val selected: MutableState<Boolean>,val icon: DrawableResource)
