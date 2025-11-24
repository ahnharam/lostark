declare module '@/assets/BuffImage' {
  export const ENGRAVING_ICON_MAP: Record<string, string>
  export function getEngravingIcon(name?: string): string
  const defaultExport: typeof ENGRAVING_ICON_MAP
  export default defaultExport
}
