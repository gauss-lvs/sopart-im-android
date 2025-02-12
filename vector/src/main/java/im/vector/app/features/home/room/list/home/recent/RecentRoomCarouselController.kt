/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.home.room.list.home.recent

import android.content.res.Resources
import android.util.TypedValue
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.CarouselModelBuilder
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.carousel
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.home.room.list.RoomListListener
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RecentRoomCarouselController @Inject constructor(
        private val avatarRenderer: AvatarRenderer,
        private val resources: Resources,
) : EpoxyController() {

    private var data: List<RoomSummary>? = null
    var listener: RoomListListener? = null

    private val hPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics
    ).toInt()

    private val itemSpacing = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            24f,
            resources.displayMetrics
    ).toInt()

    fun submitList(recentList: List<RoomSummary>) {
        this.data = recentList
        requestModelBuild()
    }

    override fun buildModels() {
        val host = this
        data?.let { data ->
            carousel {
                id("recents_carousel")
                padding(Carousel.Padding(host.hPadding, host.itemSpacing))
                withModelsFrom(data) { roomSummary ->
                    val onClick = host.listener?.let { it::onRoomClicked }
                    val onLongClick = host.listener?.let { it::onRoomLongClicked }

                    RecentRoomItem_()
                            .id(roomSummary.roomId)
                            .avatarRenderer(host.avatarRenderer)
                            .matrixItem(roomSummary.toMatrixItem())
                            .unreadNotificationCount(roomSummary.notificationCount)
                            .showHighlighted(roomSummary.highlightCount > 0)
                            .itemLongClickListener { _ -> onLongClick?.invoke(roomSummary) ?: false }
                            .itemClickListener { onClick?.invoke(roomSummary) }
                }
            }
        }
    }
}

private inline fun <T> CarouselModelBuilder.withModelsFrom(
        items: List<T>,
        modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}
