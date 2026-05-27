import { TranslateHttpLoader } from "@ngx-translate/http-loader";

export function translateFactory() {
  return new TranslateHttpLoader();
}
