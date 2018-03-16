/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ycengine.yclibrary.library.recyclerviewhelper;

/**
 * Interface to notify a RecyclerView.Adapter of moving and dismissal event
 */
public interface RVHAdapter {

  /**
   * Called when an item has been dragged far enough to trigger a move. This is called every time
   * an item is shifted, and not at the end of a "drop" event.
   *
   * Implementations should call RecyclerView.Adapter notifyItemMoved(int, int) after
   * adjusting the underlying data to reflect this move.
   *
   * @param fromPosition
   *     The start position of the moved item.
   * @param toPosition
   *     Then resolved position of the moved item.
   * @return True if the item was moved to the new adapter position.
   */
  boolean onItemMove(int fromPosition, int toPosition);

  /**
   * Called when an item has been dismissed by a swipe.
   *
   * Implementations should call RecyclerView.Adapter notifyItemRemoved(int) after
   * adjusting the underlying data to reflect this removal.
   *
   * @param position
   *     The position of the item dismissed.
   * @param direction
   *     the direction
   */
  void onItemDismiss(int position, int direction);
}
