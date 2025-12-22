declare module 'muuri' {
  export type MuuriItem = {
    getElement(): HTMLElement
  }

  export type MuuriLayoutOptions = {
    fillGaps?: boolean
    horizontal?: boolean
    rounding?: boolean
    alignRight?: boolean
    alignBottom?: boolean
  }

  export type MuuriDragSortPredicate = {
    threshold?: number
  }

  export type MuuriOptions = {
    items?: string
    layout?: MuuriLayoutOptions
    dragEnabled?: boolean
    dragHandle?: string
    dragSort?: boolean
    dragSortPredicate?: MuuriDragSortPredicate
    layoutOnInit?: boolean
  }

  export default class Muuri {
    constructor(element: HTMLElement | string, options?: MuuriOptions)
    getItems(): MuuriItem[]
    refreshItems(items?: MuuriItem[], force?: boolean): this
    layout(instant?: boolean): this
    on(eventName: string, callback: (...args: unknown[]) => void): this
    destroy(removeElements?: boolean): void
  }
}
