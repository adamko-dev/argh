plugins {
  buildsrc.`kotlin-lib`
}

description = "Internal Argh library. API for ${projects.modules.libAssetUploader.name}."

dependencies {
  compileOnly(projects.modules.libGmm)
}
