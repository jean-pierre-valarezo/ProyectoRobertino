#include "my_application.h"



#include <iostream>
#include <cstdio>
#include <memory>
#include <stdexcept>
#include <string>
#include <array>
#include <sstream> // ← Esta línea es crucial

#include <vector>


#include <flutter_linux/flutter_linux.h>
#ifdef GDK_WINDOWING_X11
#include <gdk/gdkx.h>
#endif

#include "flutter/generated_plugin_registrant.h"

#include <unistd.h>
#include <sys/types.h>


std::string ejecutarComando(const char* comando) {
    std::array<char, 128> buffer;
    std::string resultado;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(comando, "r"), pclose);
    
    if (!pipe) {
        throw std::runtime_error("No se pudo ejecutar el comando.");
    }

    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        resultado += buffer.data();
    }

    return resultado;
}


std::string ejecutarAPI(const char* comando) {
    pid_t pid = fork();

    if (pid == 0) {
        // Proceso hijo → ejecuta el script y se independiza del proceso padre
        setsid(); // Opcional: separa del terminal
        execl("/bin/sh", "sh", "-c", comando, (char *)0);
        _exit(EXIT_FAILURE); // Solo si execl falla
    } else if (pid < 0) {
        return "Error al hacer fork";
    }

    // Proceso padre continúa
    return "true";
}



bool esInterfazVirtual(const std::string& linea) {
    static std::vector<std::string> interfacesIgnoradas = {
        "docker", "br-", "virbr0", "lo", "veth", "eno"
    };
    for (const auto& nombre : interfacesIgnoradas) {
        if (linea.find(nombre) != std::string::npos) {
            return true;
        }
    }
    return false;
}

std::string extraerIP(const std::string& salida) {
    std::istringstream iss(salida);
    std::string linea;
    bool interfazValida = false;

    while (std::getline(iss, linea)) {
        // Detecta si es inicio de una interfaz
        if (linea.find(": flags=") != std::string::npos) {
            interfazValida = !esInterfazVirtual(linea);
            continue;
        }

        if (interfazValida &&
            linea.find("inet ") != std::string::npos &&
            linea.find("127.0.0.1") == std::string::npos) {

            std::istringstream lineaStream(linea);
            std::string palabra;
            while (lineaStream >> palabra) {
                if (palabra == "inet") {
                    std::string ip;
                    lineaStream >> ip;
                    return ip;
                }
            }
        }
    }

    return "No se encontró una IP válida";
}

std::string obtenerIPDesdeRuta() {
    std::array<char, 128> buffer;
    std::string resultado;

    const char* comando = "ip route get 1.1.1.1 | grep -oP 'src \\K[\\d.]+'";
    FILE* pipe = popen(comando, "r");

    if (!pipe) {
        return "0.0.0.0";
    }

    while (fgets(buffer.data(), buffer.size(), pipe) != nullptr) {
        resultado += buffer.data();
    }

    pclose(pipe);

    if (!resultado.empty() && resultado.back() == '\n') {
        resultado.pop_back();
    }

    // Si no encontró nada útil, retorna IP nula
    if (resultado.empty()) {
        return "0.0.0.0";
    }

    return resultado;
}

std::string obtenerIPInteligente() {
    std::string ip = obtenerIPDesdeRuta();

    if (ip == "0.0.0.0" || ip.empty()) {
        std::string salida = ejecutarComando("ifconfig");
        ip = extraerIP(salida);
    }

    if (ip.empty()) {
        return "0.0.0.0";
    }

    return ip;
}



struct _MyApplication
{
  GtkApplication parent_instance;
  char **dart_entrypoint_arguments;
};

G_DEFINE_TYPE(MyApplication, my_application, GTK_TYPE_APPLICATION)

static void respond(FlMethodCall *method_call,
                    FlMethodResponse *response)
{
  g_autoptr(GError) error = nullptr;
  if (!fl_method_call_respond(method_call, response, &error))
  {
    g_warning("Failed to send method call response: %s", error->message);
  }
}

static void method_call_cb(FlMethodChannel *channel,
                           FlMethodCall *method_call,
                           gpointer user_data)
{
  const gchar *method = fl_method_call_get_name(method_call);
  //if(strcmp(method))
  if (strcmp(method, "getIp") == 0)
  {
    FlValue* args = fl_method_call_get_args(method_call);
    FlValue *text_value = fl_value_lookup_string(args, "name");
    if (text_value == nullptr ||
        fl_value_get_type(text_value) != FL_VALUE_TYPE_STRING)
    {

      g_autoptr(FlMethodResponse) response = FL_METHOD_RESPONSE(fl_method_error_response_new(
          "myerr", "Argument map missing or malformed", nullptr));
      respond(method_call, response);
    }
    else
    {
      std::string ip = obtenerIPInteligente();


      FlValue *res = fl_value_new_string(ip.c_str());
      g_autoptr(FlMethodResponse) response = FL_METHOD_RESPONSE(fl_method_success_response_new(res));
      respond(method_call, response);
    }
  }else if (strcmp(method, "prenderAPI") == 0)
  {
    FlValue* args = fl_method_call_get_args(method_call);
    FlValue *text_value = fl_value_lookup_string(args, "name");
    if (text_value == nullptr ||
        fl_value_get_type(text_value) != FL_VALUE_TYPE_STRING)
    {

      g_autoptr(FlMethodResponse) response = FL_METHOD_RESPONSE(fl_method_error_response_new(
          "myerr", "Argument map missing or malformed", nullptr));
      respond(method_call, response);
    }
    else
    {
      
      std::string salida = ejecutarAPI("/home/jorge/Desktop/API.sh");


      FlValue *res = fl_value_new_string(salida.c_str());
      g_autoptr(FlMethodResponse) response = FL_METHOD_RESPONSE(fl_method_success_response_new(res));
      
      respond(method_call, response);
    }
  }
  else
  {

    g_autoptr(FlMethodResponse) response = FL_METHOD_RESPONSE(fl_method_not_implemented_response_new());
    respond(method_call, response);
  }

  
}

// Implements GApplication::activate.
static void my_application_activate(GApplication *application)
{
  MyApplication *self = MY_APPLICATION(application);
  GtkWindow *window =
      GTK_WINDOW(gtk_application_window_new(GTK_APPLICATION(application)));

  // Use a header bar when running in GNOME as this is the common style used
  // by applications and is the setup most users will be using (e.g. Ubuntu
  // desktop).
  // If running on X and not using GNOME then just use a traditional title bar
  // in case the window manager does more exotic layout, e.g. tiling.
  // If running on Wayland assume the header bar will work (may need changing
  // if future cases occur).
  gboolean use_header_bar = TRUE;
#ifdef GDK_WINDOWING_X11
  GdkScreen *screen = gtk_window_get_screen(window);
  if (GDK_IS_X11_SCREEN(screen))
  {
    const gchar *wm_name = gdk_x11_screen_get_window_manager_name(screen);
    if (g_strcmp0(wm_name, "GNOME Shell") != 0)
    {
      use_header_bar = FALSE;
    }
  }
#endif
  if (use_header_bar)
  {
    GtkHeaderBar *header_bar = GTK_HEADER_BAR(gtk_header_bar_new());
    gtk_widget_show(GTK_WIDGET(header_bar));
    gtk_header_bar_set_title(header_bar, "Robertiño");
    gtk_header_bar_set_show_close_button(header_bar, TRUE);
    gtk_window_set_titlebar(window, GTK_WIDGET(header_bar));
  }
  else
  {
    gtk_window_set_title(window, "Robertiño");
  }

  gtk_window_set_default_size(window, 1280, 720);
  gtk_widget_show(GTK_WIDGET(window));

  g_autoptr(FlDartProject) project = fl_dart_project_new();
  fl_dart_project_set_dart_entrypoint_arguments(project, self->dart_entrypoint_arguments);

  FlView *view = fl_view_new(project);
  gtk_widget_show(GTK_WIDGET(view));
  gtk_container_add(GTK_CONTAINER(window), GTK_WIDGET(view));

  fl_register_plugins(FL_PLUGIN_REGISTRY(view));

  FlEngine *engine = fl_view_get_engine(view);

  g_autoptr(FlStandardMethodCodec) codec = fl_standard_method_codec_new();
  g_autoptr(FlBinaryMessenger) messenger = fl_engine_get_binary_messenger(engine);
  g_autoptr(FlMethodChannel) channel =
      fl_method_channel_new(messenger,
                            "ip.channel/ifconfig",
                            FL_METHOD_CODEC(codec));
  fl_method_channel_set_method_call_handler(channel, method_call_cb,
                                            g_object_ref(view),
                                            g_object_unref);

  //fl_method_channel_set_method_call_handler(channel, method_call_cb, g_object_ref(view), g_object_unref);

  gtk_widget_grab_focus(GTK_WIDGET(view));
}

// Implements GApplication::local_command_line.
static gboolean my_application_local_command_line(GApplication *application, gchar ***arguments, int *exit_status)
{
  MyApplication *self = MY_APPLICATION(application);
  // Strip out the first argument as it is the binary name.
  self->dart_entrypoint_arguments = g_strdupv(*arguments + 1);

  g_autoptr(GError) error = nullptr;
  if (!g_application_register(application, nullptr, &error))
  {
    g_warning("Failed to register: %s", error->message);
    *exit_status = 1;
    return TRUE;
  }

  g_application_activate(application);
  *exit_status = 0;

  return TRUE;
}

// Implements GObject::dispose.
static void my_application_dispose(GObject *object)
{
  MyApplication *self = MY_APPLICATION(object);
  g_clear_pointer(&self->dart_entrypoint_arguments, g_strfreev);
  G_OBJECT_CLASS(my_application_parent_class)->dispose(object);
}

static void my_application_class_init(MyApplicationClass *klass)
{
  G_APPLICATION_CLASS(klass)->activate = my_application_activate;
  G_APPLICATION_CLASS(klass)->local_command_line = my_application_local_command_line;
  G_OBJECT_CLASS(klass)->dispose = my_application_dispose;
}

static void my_application_init(MyApplication *self) {}

MyApplication *my_application_new()
{
  return MY_APPLICATION(g_object_new(my_application_get_type(),
                                     "application-id", APPLICATION_ID,
                                     "flags", G_APPLICATION_NON_UNIQUE,
                                     nullptr));
}