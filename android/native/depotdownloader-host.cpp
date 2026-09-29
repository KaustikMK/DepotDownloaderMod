// Minimal Android host for the self-contained .NET DepotDownloader publish.
// The Android RID intentionally has no apphost, so this starts hostfxr from
// the directory copied out of the APK and forwards all command-line arguments.
#include <dlfcn.h>
#include <libgen.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "hostfxr.h"

int main(int argc, char *argv[])
{
    char executable[PATH_MAX];
    if (realpath(argv[0], executable) == NULL) {
        perror("Unable to resolve depotdownloader path");
        return 1;
    }

    char *directory = dirname(executable);
    if (chdir(directory) != 0) {
        perror("Unable to enter depotdownloader directory");
        return 1;
    }

    void *library = dlopen("./libhostfxr.so", RTLD_LAZY | RTLD_LOCAL);
    if (library == NULL) {
        fprintf(stderr, "Unable to load libhostfxr.so: %s\n", dlerror());
        return 1;
    }

    hostfxr_initialize_for_dotnet_command_line_fn initialize =
        (hostfxr_initialize_for_dotnet_command_line_fn)dlsym(library, "hostfxr_initialize_for_dotnet_command_line");
    hostfxr_run_app_fn run_app = (hostfxr_run_app_fn)dlsym(library, "hostfxr_run_app");
    hostfxr_close_fn close = (hostfxr_close_fn)dlsym(library, "hostfxr_close");
    if (initialize == NULL || run_app == NULL || close == NULL) {
        fprintf(stderr, "The bundled hostfxr library does not expose the required hosting APIs.\n");
        dlclose(library);
        return 1;
    }

    char managed_assembly[PATH_MAX];
    snprintf(managed_assembly, sizeof(managed_assembly), "%s/DepotDownloaderMod.dll", directory);
    const char **managed_args = static_cast<const char **>(calloc((size_t)argc, sizeof(*managed_args)));
    if (managed_args == NULL) {
        fprintf(stderr, "Unable to allocate command-line arguments.\n");
        dlclose(library);
        return 1;
    }
    managed_args[0] = managed_assembly;
    for (int index = 1; index < argc; index++) {
        managed_args[index] = argv[index];
    }

    hostfxr_handle context = NULL;
    int result = initialize(argc, managed_args, NULL, &context);
    free(managed_args);
    if (result != 0 || context == NULL) {
        fprintf(stderr, "Unable to initialize the bundled .NET runtime (0x%x).\n", result);
        dlclose(library);
        return result == 0 ? 1 : result;
    }

    result = run_app(context);
    close(context);
    dlclose(library);
    return result;
}
