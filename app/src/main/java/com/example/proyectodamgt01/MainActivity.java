package com.example.proyectodamgt01;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectodamgt01.database.AppDatabase;
import com.example.proyectodamgt01.entities.Cliente;
import com.example.proyectodamgt01.entities.DetallePedido;
import com.example.proyectodamgt01.entities.Pedido;
import com.example.proyectodamgt01.entities.Producto;
import com.example.proyectodamgt01.entities.Usuario;
import com.example.proyectodamgt01.relations.DetalleConProducto;
import com.example.proyectodamgt01.relations.PedidoConDetalles;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * MainActivity funciona como controlador principal de la app.
 *
 * En este proyecto usamos XML + Java + Room:
 * - Los layouts definen la vista.
 * - MainActivity conecta botones, formularios y tablas.
 * - Los DAO hacen las operaciones contra Room/SQLite.
 *
 * Por ahora se usa una sola Activity que cambia de pantalla con setContentView().
 * Esto mantiene el proyecto simple para clase, aunque mas adelante se podria separar
 * en Activities o Fragments por modulo.
 */
public class MainActivity extends AppCompatActivity {
    // Claves usadas para guardar la sesion local con SharedPreferences.
    private static final String PREFS_NAME = "sesion_pupuseria";
    private static final String KEY_USER_ID = "usuario_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REMEMBER = "recordar";

    // Accesos globales a base de datos, preferencias y tareas en segundo plano.
    private AppDatabase db;
    private SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    // Estado temporal de la sesion y de formularios en modo edicion.
    private int currentUserId = -1;
    private String currentUsername = "";
    private Integer productoEditandoId = null;
    private Integer usuarioEditandoId = null;
    private Integer pedidoEditandoId = null;

    // Mapa usado en la pantalla Ordenar: id_producto -> cantidad seleccionada.
    private final Map<Integer, Integer> cantidadesPedido = new HashMap<>();
    private List<Producto> productosOrden = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Room se obtiene como singleton para toda la app.
        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Asegura datos minimos aunque la base ya existiera antes de agregar la precarga.
        ensureDefaultData();

        if (prefs.getBoolean(KEY_REMEMBER, false)) {
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
            currentUsername = prefs.getString(KEY_USERNAME, "");
            showMenu();
        } else {
            showLogin();
        }
    }

    private void ensureDefaultData() {
        executor.execute(() -> {
            // Si la tabla Producto esta vacia, se cargan las pupusas base.
            if (db.productoDao().contar() == 0) {
                db.productoDao().insertar(new Producto("Queso", 0.50, 1));
                db.productoDao().insertar(new Producto("Frijol con queso", 0.50, 1));
                db.productoDao().insertar(new Producto("Revueltas", 0.60, 1));
                db.productoDao().insertar(new Producto("Loroco con queso", 0.75, 1));
                db.productoDao().insertar(new Producto("Chicharron", 0.65, 1));
            }

            // Usuario administrador inicial para poder entrar: admin / admin.
            if (db.usuarioDao().contar() == 0) {
                db.usuarioDao().insertar(new Usuario("admin", "admin", 1));
            }
        });
    }

    private void setScreen(int layoutId) {
        // Cambia el XML visible. Despues de llamar este metodo hay que volver a buscar
        // los botones/campos con findViewById(), porque la vista anterior ya no existe.
        setContentView(layoutId);
        applyInsets();
    }

    private void applyInsets() {
        // Buscamos el contenedor raíz de la pantalla actual (id="main")
        View main = findViewById(R.id.main);
        if (main == null) return;

        // Configuramos el escuchador de Insets para manejar el espacio de las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            // Obtenemos el tamaño de las barras (estado arriba, navegación abajo)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Aplicamos el padding al contenedor principal para "empujar" el contenido hacia adentro
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            
            // IMPORTANTE: Retornamos CONSUMED para que el sistema sepa que ya nos encargamos del espacio
            // y no intente reajustar la vista por su cuenta al interactuar con formularios.
            return WindowInsetsCompat.CONSUMED;
        });

        // Forzamos al sistema a aplicar los márgenes de inmediato para evitar el "salto" visual
        main.requestApplyInsets();
    }

    private void setupNavigation() {
        // La barra superior se incluye en varias pantallas. Como no todas la tienen,
        // se validan nulls antes de asignar los listeners.
        View navMenu = findViewById(R.id.navMenu);
        View navOrder = findViewById(R.id.navOrder);
        View navPending = findViewById(R.id.navPending);
        View navDelivered = findViewById(R.id.navDelivered);
        View navUsers = findViewById(R.id.navUsers);
        View navLogout = findViewById(R.id.navLogout);

        if (navMenu != null) navMenu.setOnClickListener(v -> showMenu());
        if (navOrder != null) navOrder.setOnClickListener(v -> showOrdenar());
        if (navPending != null) navPending.setOnClickListener(v -> showPendientes());
        if (navDelivered != null) navDelivered.setOnClickListener(v -> showEntregados());
        if (navUsers != null) navUsers.setOnClickListener(v -> showUsuarios());
        if (navLogout != null) navLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Limpia la sesion en SharedPreferences y regresa al Login.
        prefs.edit()
                .putBoolean(KEY_REMEMBER, false)
                .putInt(KEY_USER_ID, -1)
                .putString(KEY_USERNAME, "")
                .apply();
        
        currentUserId = -1;
        currentUsername = "";
        showLogin();
    }

    private void showLogin() {
        // Pantalla inicial: valida usuario activo y password desde Room.
        setScreen(R.layout.login);

        EditText edtUsuario = findViewById(R.id.edtUsuario);
        EditText edtPassword = findViewById(R.id.edtPassword);
        Button btnIngresar = findViewById(R.id.btnIngresar);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        Button btnSalir = findViewById(R.id.btnSalirApp);

        btnIngresar.setOnClickListener(v -> {
            String username = textOf(edtUsuario);
            String password = textOf(edtPassword);

            if (username.isEmpty() || password.isEmpty()) {
                toast("Ingresa usuario y contrasena");
                return;
            }

            executor.execute(() -> {
                // Las consultas a Room se ejecutan fuera del hilo principal.
                Usuario usuario = db.usuarioDao().login(username, password);
                runOnUiThread(() -> {
                    if (usuario == null) {
                        toast("Usuario inactivo o credenciales incorrectas");
                        return;
                    }

                    currentUserId = usuario.idUsuario;
                    currentUsername = usuario.username;

                    boolean recordar = ((android.widget.CheckBox) findViewById(R.id.chkRecordarSesion)).isChecked();
                    // SharedPreferences guarda la sesion solo si el usuario marca recordar.
                    prefs.edit()
                            .putBoolean(KEY_REMEMBER, recordar)
                            .putInt(KEY_USER_ID, usuario.idUsuario)
                            .putString(KEY_USERNAME, usuario.username)
                            .apply();

                    showMenu();
                });
            });
        });

        btnRegistrar.setOnClickListener(v -> showNuevoUsuario());
        if (btnSalir != null) btnSalir.setOnClickListener(v -> finish());
    }

    private void showNuevoUsuario() {
        // Registro publico: permite solicitar acceso, pero no activa al usuario.
        setScreen(R.layout.nuevo_usuario);

        EditText edtUsername = findViewById(R.id.edtNuevoUsername);
        EditText edtPassword = findViewById(R.id.edtNuevaPassword);
        EditText edtConfirmar = findViewById(R.id.edtConfirmarPassword);
        Button btnRegistrar = findViewById(R.id.btnRegistrarUsuario);
        Button btnVolver = findViewById(R.id.btnVolverLogin);

        btnRegistrar.setOnClickListener(v -> {
            String username = textOf(edtUsername);
            String password = textOf(edtPassword);
            String confirmar = textOf(edtConfirmar);

            if (username.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
                toast("Completa todos los campos");
                return;
            }

            if (!password.equals(confirmar)) {
                toast("Las contrasenas no coinciden");
                return;
            }

            executor.execute(() -> {
                if (db.usuarioDao().contarPorUsername(username) > 0) {
                    runOnUiThread(() -> toast("Ese usuario ya existe"));
                    return;
                }

                // Registro publico: todo usuario nuevo queda inactivo hasta que admin lo apruebe.
                db.usuarioDao().insertar(new Usuario(username, password, 0));
                runOnUiThread(this::showUsuarioPendienteDialog);
            });
        });

        btnVolver.setOnClickListener(v -> showLogin());
    }

    private void showUsuarioPendienteDialog() {
        // Dialogo informativo tras crear un usuario inactivo.
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_usuario_pendiente, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        Button btnAceptar = dialogView.findViewById(R.id.btnAceptarUsuarioPendiente);
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            showLogin();
        });
        dialog.show();
    }

    private void showMenu() {
        // Modulo de mantenimiento de productos/pupusas.
        productoEditandoId = null;
        setScreen(R.layout.menu);
        setupNavigation();
        setupProductosCrud();
    }

    private void setupProductosCrud() {
        // Conecta formulario, buscador y tabla del CRUD de productos.
        EditText edtNombre = findViewById(R.id.edtNombreProducto);
        EditText edtPrecio = findViewById(R.id.edtPrecioProducto);
        EditText edtBuscar = findViewById(R.id.edtBuscarProducto);
        View btnGuardar = findViewById(R.id.btnAgregarPupusa);

        btnGuardar.setOnClickListener(v -> guardarProducto(edtNombre, edtPrecio));
        edtBuscar.addTextChangedListener(simpleTextWatcher(() -> cargarProductos(textOf(edtBuscar))));
        cargarProductos("");
    }

    private void guardarProducto(EditText edtNombre, EditText edtPrecio) {
        // Inserta un producto nuevo o actualiza el que se selecciono con editar.
        String nombre = textOf(edtNombre);
        String precioTexto = textOf(edtPrecio);

        if (nombre.isEmpty() || precioTexto.isEmpty()) {
            toast("Completa producto y precio");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioTexto);
        } catch (NumberFormatException e) {
            toast("Precio invalido");
            return;
        }

        executor.execute(() -> {
            if (productoEditandoId == null) {
                // Nuevo producto: por defecto nace activo.
                db.productoDao().insertar(new Producto(nombre, precio, 1));
            } else {
                // Producto existente: se conserva su estado y se actualizan datos basicos.
                Producto producto = db.productoDao().buscarPorId(productoEditandoId);
                if (producto != null) {
                    producto.nombre = nombre;
                    producto.precio = precio;
                    db.productoDao().actualizar(producto);
                }
            }

            runOnUiThread(() -> {
                productoEditandoId = null;
                edtNombre.setText("");
                edtPrecio.setText("");
                cargarProductos("");
                toast("Producto guardado");
            });
        });
    }

    private void cargarProductos(String filtro) {
        // Carga productos desde Room y aplica el filtro de busqueda en memoria.
        executor.execute(() -> {
            List<Producto> productos = db.productoDao().listarTodos();
            List<Producto> filtrados = new ArrayList<>();
            for (Producto producto : productos) {
                if (filtro == null || filtro.isEmpty() || producto.nombre.toLowerCase().contains(filtro.toLowerCase())) {
                    filtrados.add(producto);
                }
            }
            runOnUiThread(() -> renderProductos(filtrados));
        });
    }

    private void renderProductos(List<Producto> productos) {
        // Redibuja la tabla del menu con los productos obtenidos de SQLite.
        TableLayout table = findViewById(R.id.tablePupusas);
        if (table == null) return;
        clearTableBody(table);

        for (Producto producto : productos) {
            TableRow row = new TableRow(this);
            boolean activo = producto.estado == 1;
            row.addView(cell(producto.nombre, activo));
            row.addView(cell(currency.format(producto.precio), activo));
            row.addView(actionCell(
                    () -> editarProducto(producto),
                    () -> eliminarProducto(producto),
                    () -> cambiarEstadoProducto(producto)
            ));
            table.addView(row);
        }
    }

    private void editarProducto(Producto producto) {
        // Carga el producto seleccionado en el formulario superior.
        productoEditandoId = producto.idProducto;
        ((EditText) findViewById(R.id.edtNombreProducto)).setText(producto.nombre);
        ((EditText) findViewById(R.id.edtPrecioProducto)).setText(String.valueOf(producto.precio));
    }

    private void eliminarProducto(Producto producto) {
        // Si el producto ya fue usado en pedidos, la FK impide eliminarlo.
        executor.execute(() -> {
            try {
                db.productoDao().eliminar(producto);
                runOnUiThread(() -> {
                    cargarProductos("");
                    toast("Producto eliminado");
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("No se puede eliminar: tiene pedidos asociados"));
            }
        });
    }

    private void cambiarEstadoProducto(Producto producto) {
        // Alterna entre activo/inactivo. Los inactivos se muestran con fila roja.
        executor.execute(() -> {
            db.productoDao().cambiarEstado(producto.idProducto, producto.estado == 1 ? 0 : 1);
            runOnUiThread(() -> cargarProductos(""));
        });
    }

    private void showUsuarios() {
        // Modulo de administracion de usuarios y aprobacion de accesos.
        usuarioEditandoId = null;
        setScreen(R.layout.usuarios);
        setupNavigation();
        setupUsuariosCrud();
    }

    private void setupUsuariosCrud() {
        // Conecta formulario, buscador y tabla del CRUD de usuarios.
        EditText edtUsername = findViewById(R.id.edtUsernameAdmin);
        EditText edtPassword = findViewById(R.id.edtPasswordAdmin);
        EditText edtBuscar = findViewById(R.id.edtBuscarUsuario);
        View btnGuardar = findViewById(R.id.btnAgregarUsuario);

        btnGuardar.setOnClickListener(v -> guardarUsuario(edtUsername, edtPassword));
        edtBuscar.addTextChangedListener(simpleTextWatcher(() -> cargarUsuarios(textOf(edtBuscar))));
        cargarUsuarios("");
    }

    private void guardarUsuario(EditText edtUsername, EditText edtPassword) {
        // Usuarios creados por admin tambien nacen inactivos para aprobarlos manualmente.
        String username = textOf(edtUsername);
        String password = textOf(edtPassword);

        if (username.isEmpty() || password.isEmpty()) {
            toast("Completa usuario y contrasena");
            return;
        }

        executor.execute(() -> {
            if (usuarioEditandoId == null && db.usuarioDao().contarPorUsername(username) > 0) {
                runOnUiThread(() -> toast("Ese usuario ya existe"));
                return;
            }

            if (usuarioEditandoId == null) {
                db.usuarioDao().insertar(new Usuario(username, password, 0));
            } else {
                Usuario usuario = db.usuarioDao().buscarPorId(usuarioEditandoId);
                if (usuario != null) {
                    usuario.username = username;
                    usuario.password = password;
                    db.usuarioDao().actualizar(usuario);
                }
            }

            runOnUiThread(() -> {
                usuarioEditandoId = null;
                edtUsername.setText("");
                edtPassword.setText("");
                cargarUsuarios("");
                toast("Usuario guardado");
            });
        });
    }

    private void cargarUsuarios(String filtro) {
        executor.execute(() -> {
            List<Usuario> usuarios = db.usuarioDao().listarTodos();
            List<Usuario> filtrados = new ArrayList<>();
            for (Usuario usuario : usuarios) {
                if (filtro == null || filtro.isEmpty() || usuario.username.toLowerCase().contains(filtro.toLowerCase())) {
                    filtrados.add(usuario);
                }
            }
            runOnUiThread(() -> renderUsuarios(filtrados));
        });
    }

    private void renderUsuarios(List<Usuario> usuarios) {
        // Renderiza usuarios desde SQLite. estado=0 se pinta como fila inactiva.
        TableLayout table = findViewById(R.id.tableUsuarios);
        if (table == null) return;
        clearTableBody(table);

        for (Usuario usuario : usuarios) {
            TableRow row = new TableRow(this);
            boolean activo = usuario.estado == 1;
            row.addView(cell(usuario.username, activo));
            row.addView(cell(activo ? "Activo" : "Inactivo", activo));
            row.addView(actionCell(
                    () -> editarUsuario(usuario),
                    () -> eliminarUsuario(usuario),
                    () -> cambiarEstadoUsuario(usuario)
            ));
            table.addView(row);
        }
    }

    private void editarUsuario(Usuario usuario) {
        usuarioEditandoId = usuario.idUsuario;
        ((EditText) findViewById(R.id.edtUsernameAdmin)).setText(usuario.username);
        ((EditText) findViewById(R.id.edtPasswordAdmin)).setText(usuario.password);
    }

    private void eliminarUsuario(Usuario usuario) {
        // Proteccion basica: no permitir que el usuario conectado se elimine a si mismo.
        if (usuario.idUsuario == currentUserId) {
            toast("No puedes eliminar tu propia sesion");
            return;
        }

        executor.execute(() -> {
            try {
                db.usuarioDao().eliminar(usuario);
                runOnUiThread(() -> {
                    cargarUsuarios("");
                    toast("Usuario eliminado");
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("No se puede eliminar: tiene pedidos asociados"));
            }
        });
    }

    private void cambiarEstadoUsuario(Usuario usuario) {
        // Proteccion basica: no permitir desactivar la sesion actual.
        if (usuario.idUsuario == currentUserId) {
            toast("No puedes desactivar tu propia sesion");
            return;
        }

        executor.execute(() -> {
            db.usuarioDao().cambiarEstado(usuario.idUsuario, usuario.estado == 1 ? 0 : 1);
            runOnUiThread(() -> cargarUsuarios(""));
        });
    }

    private void showOrdenar() {
        // Inicia una orden nueva, limpiando cantidades y pedido en edicion.
        pedidoEditandoId = null;
        cantidadesPedido.clear();
        abrirPantallaOrden();
    }

    private void editarPedido(PedidoConDetalles pedido) {
        // Reabre una orden pendiente con sus cantidades para modificarla.
        pedidoEditandoId = pedido.pedido.idPedido;
        cantidadesPedido.clear();

        if (pedido.detalles != null) {
            for (DetalleConProducto detalle : pedido.detalles) {
                cantidadesPedido.put(detalle.detallePedido.productoId, detalle.detallePedido.cantidad);
            }
        }

        abrirPantallaOrden();
    }

    private void abrirPantallaOrden() {
        // Configura botones de la pantalla Ordenar y carga productos activos.
        setScreen(R.layout.ordenar);
        setupNavigation();

        Button btnReiniciar = findViewById(R.id.btnReiniciarOrden);
        Button btnConfirmar = findViewById(R.id.btnConfirmarPedido);
        btnReiniciar.setOnClickListener(v -> {
            cantidadesPedido.clear();
            renderOrden();
        });
        btnConfirmar.setOnClickListener(v -> confirmarPedido());

        cargarProductosOrden();
    }

    private void cargarProductosOrden() {
        // En ordenes solo se venden productos activos.
        executor.execute(() -> {
            productosOrden = db.productoDao().listarActivos();
            runOnUiThread(this::renderOrden);
        });
    }

    private void renderOrden() {
        // Redibuja la tabla de la orden y recalcula subtotales/total.
        LinearLayout table = findViewById(R.id.tableOrden);
        if (table == null) return;
        clearLinearBody(table);

        double total = 0;
        for (Producto producto : productosOrden) {
            int cantidad = cantidadesPedido.getOrDefault(producto.idProducto, 0);
            double subtotal = cantidad * producto.precio;
            total += subtotal;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(productOrderCell(producto));
            row.addView(orderSubtotalCell(currency.format(subtotal)));
            row.addView(quantityCell(producto, cantidad));
            table.addView(row);
        }

        ((TextView) findViewById(R.id.txtTotalPedido)).setText("TOTAL: " + currency.format(total));
    }

    private void confirmarPedido() {
        // Abre el dialogo donde se capturan datos del cliente antes de guardar.
        try {
            double total = calcularTotalPedidoActual();
            if (total <= 0) {
                toast("Selecciona al menos una pupusa");
                return;
            }

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmar_pedido, null);
            EditText edtNombre = dialogView.findViewById(R.id.edtNombreClienteConfirm);
            EditText edtTelefono = dialogView.findViewById(R.id.edtTelefonoClienteConfirm);
            TextView txtTotal = dialogView.findViewById(R.id.txtTotalConfirm);
            LinearLayout container = dialogView.findViewById(R.id.containerResumenItems);
            Button btnGuardar = dialogView.findViewById(R.id.btnGuardarPedido);
            Button btnCerrar = dialogView.findViewById(R.id.btnCerrarDialog);

            if (edtNombre == null || edtTelefono == null || txtTotal == null || container == null || btnGuardar == null || btnCerrar == null) {
                toast("No se pudo abrir la confirmacion del pedido");
                return;
            }

            txtTotal.setText("Total: " + currency.format(total));
            renderResumenDialog(container);

            AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
            btnGuardar.setOnClickListener(v -> guardarPedido(dialog, edtNombre, edtTelefono));
            dialog.show();
        } catch (Exception e) {
            toast("Error al confirmar pedido: " + e.getMessage());
        }
    }

    private void guardarPedido(AlertDialog dialog, EditText edtNombre, EditText edtTelefono) {
        // Guarda cabecera Pedido y sus DetallePedido. Si es edicion, reemplaza detalles.
        String nombre = textOf(edtNombre);
        String telefono = textOf(edtTelefono);
        if (nombre.isEmpty() || telefono.isEmpty()) {
            toast("Completa nombre y telefono del cliente");
            return;
        }

        executor.execute(() -> {
            try {
                int vendedorId = obtenerUsuarioVendedorId();
                if (vendedorId <= 0) {
                    runOnUiThread(() -> toast("No hay usuario activo para guardar el pedido"));
                    return;
                }

                Cliente cliente = db.clienteDao().buscarPorTelefono(telefono);
                int clienteId;
                if (cliente == null) {
                    // Cliente nuevo.
                    clienteId = (int) db.clienteDao().insertar(new Cliente(nombre, telefono, ""));
                } else {
                    // Cliente existente: se actualiza el nombre por si cambio.
                    cliente.nombre = nombre;
                    db.clienteDao().actualizar(cliente);
                    clienteId = cliente.idCliente;
                }

                String fecha = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
                String hora = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
                double total = calcularTotalPedidoActual();

                int pedidoId;
                if (pedidoEditandoId == null) {
                    // Pedido nuevo en estado 0: Pendiente.
                    pedidoId = (int) db.pedidoDao().insertar(new Pedido(vendedorId, clienteId, fecha, hora, total, 0, 0));
                } else {
                    // Pedido existente: actualiza cabecera y recrea detalles.
                    Pedido pedido = db.pedidoDao().buscarPorId(pedidoEditandoId);
                    if (pedido == null) {
                        runOnUiThread(() -> toast("No se encontro el pedido a modificar"));
                        return;
                    }
                    pedido.usuarioId = vendedorId;
                    pedido.clienteId = clienteId;
                    pedido.fecha = fecha;
                    pedido.hora = hora;
                    pedido.total = total;
                    pedido.estadoEntrega = 0;
                    db.pedidoDao().actualizar(pedido);
                    db.detallePedidoDao().eliminarPorPedido(pedidoEditandoId);
                    pedidoId = pedidoEditandoId;
                }

                for (Producto producto : productosOrden) {
                    int cantidad = cantidadesPedido.getOrDefault(producto.idProducto, 0);
                    if (cantidad > 0) {
                        double subtotal = cantidad * producto.precio;
                        db.detallePedidoDao().insertar(new DetallePedido(pedidoId, producto.idProducto, cantidad, producto.precio, subtotal));
                    }
                }

                runOnUiThread(() -> {
                    dialog.dismiss();
                    pedidoEditandoId = null;
                    cantidadesPedido.clear();
                    renderOrden();
                    toast("Pedido guardado como pendiente");
                    showPendientes();
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("Error al guardar pedido: " + e.getMessage()));
            }
        });
    }

    private int obtenerUsuarioVendedorId() {
        // Usa el usuario conectado; si no existe por sesion vieja, usa el primer activo.
        if (currentUserId > 0) {
            return currentUserId;
        }

        Usuario usuarioActivo = db.usuarioDao().buscarPrimerActivo();
        if (usuarioActivo == null) {
            return -1;
        }

        currentUserId = usuarioActivo.idUsuario;
        currentUsername = usuarioActivo.username;
        return usuarioActivo.idUsuario;
    }

    private void showPendientes() {
        // Lista pedidos con estado_entrega = 0.
        setScreen(R.layout.pendientes);
        setupNavigation();

        EditText edtBuscar = findViewById(R.id.edtBuscarCliente);
        edtBuscar.addTextChangedListener(simpleTextWatcher(() -> cargarPedidos(0, textOf(edtBuscar))));
        cargarPedidos(0, "");
    }

    private void showEntregados() {
        // Lista pedidos con estado_entrega = 1.
        setScreen(R.layout.entregados);
        setupNavigation();

        EditText edtBuscar = findViewById(R.id.edtBuscarClienteEntregado);
        edtBuscar.addTextChangedListener(simpleTextWatcher(() -> cargarPedidos(1, textOf(edtBuscar))));
        cargarPedidos(1, "");
    }

    private void cargarPedidos(int estadoEntrega, String filtroCliente) {
        // Obtiene pedidos con Cliente, Usuario y Detalles usando relaciones de Room.
        executor.execute(() -> {
            List<PedidoConDetalles> pedidos = db.pedidoDao().listarConDetallesPorEstado(estadoEntrega);
            List<PedidoConDetalles> filtrados = new ArrayList<>();
            for (PedidoConDetalles pedido : pedidos) {
                String nombre = pedido.cliente != null ? pedido.cliente.nombre : "";
                if (filtroCliente == null || filtroCliente.isEmpty() || nombre.toLowerCase().contains(filtroCliente.toLowerCase())) {
                    filtrados.add(pedido);
                }
            }
            runOnUiThread(() -> renderPedidos(estadoEntrega, filtrados));
        });
    }

    private void renderPedidos(int estadoEntrega, List<PedidoConDetalles> pedidos) {
        // Construye dinamicamente las tarjetas de pendientes o entregados.
        LinearLayout contenedor = findViewById(estadoEntrega == 0 ? R.id.listaPedidosPendientes : R.id.listaPedidosEntregados);
        if (contenedor == null) return;
        contenedor.removeAllViews();

        double totalVendido = 0;
        for (PedidoConDetalles pedido : pedidos) {
            if (estadoEntrega == 1) totalVendido += pedido.pedido.total;
            contenedor.addView(pedidoCard(pedido, estadoEntrega));
        }

        TextView txtTotalVendido = findViewById(R.id.txtTotalVendido);
        if (txtTotalVendido != null) {
            txtTotalVendido.setText(currency.format(totalVendido));
        }
    }

    private LinearLayout pedidoCard(PedidoConDetalles pedido, int estadoEntrega) {
        // Crea una tarjeta visual de pedido con datos de cliente, detalle y acciones.
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(estadoEntrega == 0 ? R.drawable.bg_pending_card : R.drawable.bg_delivered_card);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        Cliente cliente = pedido.cliente;
        Usuario usuario = pedido.usuario;
        card.addView(label(cliente != null ? cliente.nombre : "Cliente sin nombre", 22, R.color.cyber_cyan, true));
        card.addView(label("Celular: " + (cliente != null ? cliente.telefono : "N/D"), 13, R.color.cyber_text_muted, false));
        card.addView(detallesTable(pedido.detalles));
        card.addView(label("Total: " + currency.format(pedido.pedido.total), 20, R.color.cyber_yellow, true));
        card.addView(metaLabel("Fecha: " + pedido.pedido.fecha + "  " + pedido.pedido.hora));
        card.addView(metaLabel((estadoEntrega == 1 ? "Entregado por: " : "Usuario: ") + (usuario != null ? usuario.username : currentUsername)));

        if (estadoEntrega == 0) {
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(android.view.Gravity.CENTER);
            actions.setPadding(0, dp(14), 0, 0);
            actions.addView(wideActionButton("BORRAR", R.drawable.bg_button_outline_magenta, R.color.cyber_magenta, () -> eliminarPedido(pedido.pedido.idPedido)));
            actions.addView(wideActionButton("MODIFICAR", R.drawable.bg_button_outline_yellow, R.color.cyber_yellow, () -> editarPedido(pedido)));
            actions.addView(wideActionButton("ENTREGAR", R.drawable.bg_cyber_button, R.color.black, () -> cambiarEstadoPedido(pedido.pedido.idPedido, 1)));
            card.addView(actions);
        } else {
            Button action = wideActionButton("RESTAURAR", R.drawable.bg_button_outline_yellow, R.color.cyber_yellow, () -> cambiarEstadoPedido(pedido.pedido.idPedido, 0));
            LinearLayout.LayoutParams restoreParams = new LinearLayout.LayoutParams(dp(150), dp(46));
            restoreParams.setMargins(0, dp(16), 0, 0);
            action.setLayoutParams(restoreParams);
            card.addView(action);
        }

        return card;
    }

    private Button wideActionButton(String text, int background, int color, Runnable action) {
        // Boton reutilizable para acciones grandes de las tarjetas de pedido.
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(getColor(color));
        button.setTextSize(11);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackgroundResource(background);
        button.setMinWidth(0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void eliminarPedido(int idPedido) {
        // Elimina el pedido. Los detalles se borran por CASCADE en la relacion Room.
        executor.execute(() -> {
            db.pedidoDao().eliminarPorId(idPedido);
            runOnUiThread(() -> {
                toast("Pedido eliminado");
                showPendientes();
            });
        });
    }

    private TableLayout detallesTable(List<DetalleConProducto> detalles) {
        // Tabla interna de cada tarjeta: producto, cantidad y subtotal.
        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        table.addView(pendingHeaderRow());

        if (detalles != null) {
            for (DetalleConProducto detalle : detalles) {
                TableRow row = new TableRow(this);
                String nombre = detalle.producto != null ? detalle.producto.nombre : "Producto";
                row.addView(pendingCell(nombre, false));
                row.addView(pendingCell(String.valueOf(detalle.detallePedido.cantidad), true));
                row.addView(pendingCell(currency.format(detalle.detallePedido.subtotal), true));
                table.addView(row);
            }
        }
        return table;
    }

    private void cambiarEstadoPedido(int idPedido, int nuevoEstado) {
        // 0 = pendiente, 1 = entregado. Cambiar estado mueve el pedido entre pantallas.
        executor.execute(() -> {
            db.pedidoDao().cambiarEstadoEntrega(idPedido, nuevoEstado);
            runOnUiThread(() -> {
                toast(nuevoEstado == 1 ? "Pedido entregado" : "Pedido restaurado a pendientes");
                if (nuevoEstado == 1) showPendientes();
                else showEntregados();
            });
        });
    }

    private double calcularTotalPedidoActual() {
        // Suma precio * cantidad de los productos seleccionados en la orden actual.
        double total = 0;
        for (Producto producto : productosOrden) {
            total += producto.precio * cantidadesPedido.getOrDefault(producto.idProducto, 0);
        }
        return total;
    }

    private void renderResumenDialog(LinearLayout container) {
        // Llena el dialogo de confirmacion con los productos que tienen cantidad > 0.
        container.removeAllViews();
        for (Producto producto : productosOrden) {
            int cantidad = cantidadesPedido.getOrDefault(producto.idProducto, 0);
            if (cantidad > 0) {
                TableRow row = new TableRow(this);
                row.setPadding(dp(8), dp(8), dp(8), dp(8));
                row.addView(weightedLabel(producto.nombre, 1.5f));
                row.addView(weightedLabel(String.valueOf(cantidad), 1f));
                row.addView(weightedLabel(currency.format(cantidad * producto.precio), 1f));
                container.addView(row);
            }
        }
    }

    private LinearLayout productOrderCell(Producto producto) {
        // Celda compuesta: nombre del producto y precio debajo.
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setBackgroundResource(R.drawable.bg_table_cell);
        cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(10), dp(6), dp(10), dp(6));
        cell.setLayoutParams(new LinearLayout.LayoutParams(dp(220), dp(76)));

        TextView nombre = label(producto.nombre, 14, R.color.cyber_text, true);
        nombre.setSingleLine(false);
        nombre.setMaxLines(2);
        nombre.setEllipsize(TextUtils.TruncateAt.END);
        nombre.setIncludeFontPadding(false);
        cell.addView(nombre);

        TextView precio = label(currency.format(producto.precio), 11, R.color.cyber_magenta, true);
        precio.setSingleLine(true);
        precio.setIncludeFontPadding(false);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.setMargins(0, dp(4), 0, 0);
        precio.setLayoutParams(priceParams);
        cell.addView(precio);
        return cell;
    }

    private TextView orderSubtotalCell(String text) {
        // Celda de subtotal dentro de la tabla Ordenar.
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextColor(getColor(R.color.cyber_text));
        cell.setTextSize(15);
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        cell.setBackgroundResource(R.drawable.bg_table_cell);
        cell.setSingleLine(true);
        cell.setLayoutParams(new LinearLayout.LayoutParams(dp(120), dp(76)));
        return cell;
    }

    private LinearLayout quantityCell(Producto producto, int cantidad) {
        // Celda con botones - y +. Cada clic actualiza el mapa cantidadesPedido.
        LinearLayout cell = new LinearLayout(this);
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setBackgroundResource(R.drawable.bg_table_cell);
        cell.setLayoutParams(new LinearLayout.LayoutParams(dp(176), dp(76)));

        Button minus = qtyButton("-");
        TextView value = qtyValue(String.valueOf(cantidad));
        Button plus = qtyButton("+");

        minus.setOnClickListener(v -> {
            int actual = cantidadesPedido.getOrDefault(producto.idProducto, 0);
            if (actual > 0) cantidadesPedido.put(producto.idProducto, actual - 1);
            renderOrden();
        });
        plus.setOnClickListener(v -> {
            int actual = cantidadesPedido.getOrDefault(producto.idProducto, 0);
            cantidadesPedido.put(producto.idProducto, actual + 1);
            renderOrden();
        });

        cell.addView(minus);
        cell.addView(value);
        cell.addView(plus);
        return cell;
    }

    private Button qtyButton(String text) {
        // Boton circular reutilizado por los controles de cantidad.
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(24);
        button.setTextColor(0xFF07351F);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackgroundResource(R.drawable.bg_qty_button);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
        return button;
    }

    private TextView qtyValue(String text) {
        // Caja central que muestra la cantidad seleccionada.
        TextView value = new TextView(this);
        value.setText(text);
        value.setTextColor(getColor(R.color.cyber_yellow));
        value.setTextSize(20);
        value.setGravity(android.view.Gravity.CENTER);
        value.setTypeface(null, android.graphics.Typeface.BOLD);
        value.setBackgroundResource(R.drawable.bg_qty_value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(62), dp(48));
        params.setMargins(dp(8), 0, dp(8), 0);
        value.setLayoutParams(params);
        return value;
    }

    private TextView cell(String text, boolean activo) {
        // Celda generica para tablas CRUD. Si activo=false usa fondo rojo.
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextSize(14);
        cell.setTextColor(getColor(activo ? R.color.cyber_text : R.color.white));
        cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(12), 0, dp(12), 0);
        cell.setMinWidth(dp(90));
        cell.setBackgroundResource(activo ? R.drawable.bg_table_cell : R.drawable.bg_table_cell_inactive);
        cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, dp(54)));
        return cell;
    }

    private LinearLayout actionCell(Runnable onEdit, Runnable onDelete, Runnable onToggle) {
        // Grupo de acciones reutilizable: editar, eliminar y activar/desactivar.
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(android.view.Gravity.CENTER_VERTICAL);
        actions.setBackgroundResource(R.drawable.bg_table_cell);
        actions.setPadding(dp(8), 0, dp(8), 0);
        actions.setLayoutParams(new TableRow.LayoutParams(dp(150), dp(54)));

        actions.addView(iconButton(R.drawable.ic_edit, R.drawable.bg_button_outline_cyan, onEdit));
        actions.addView(iconButton(R.drawable.ic_delete, R.drawable.bg_button_outline_magenta, onDelete));
        actions.addView(iconButton(R.drawable.ic_power, R.drawable.bg_button_outline_yellow, onToggle));
        return actions;
    }

    private ImageButton iconButton(int icon, int background, Runnable action) {
        // Crea un ImageButton compacto para las tablas CRUD.
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setBackgroundResource(background);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(36));
        params.setMargins(0, 0, dp(6), 0);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private TableRow pendingHeaderRow() {
        // Encabezado para tablas internas de pedidos.
        TableRow row = new TableRow(this);
        row.addView(pendingHeader("Pedido"));
        row.addView(pendingHeader("Cant."));
        row.addView(pendingHeader("Subtotal"));
        return row;
    }

    private TextView pendingHeader(String text) {
        // Celda de encabezado para detalle de pedido.
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(13);
        view.setTextColor(getColor(R.color.black));
        view.setGravity(android.view.Gravity.CENTER);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setBackgroundResource(R.drawable.bg_table_header);
        view.setPadding(dp(8), 0, dp(8), 0);
        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, dp(40)));
        return view;
    }

    private TextView pendingCell(String text, boolean center) {
        // Celda de datos para detalle de pedido.
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(13);
        view.setTextColor(getColor(R.color.cyber_text));
        view.setGravity(center ? android.view.Gravity.CENTER : android.view.Gravity.CENTER_VERTICAL);
        view.setBackgroundResource(R.drawable.bg_table_cell);
        view.setPadding(dp(10), 0, dp(10), 0);
        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, dp(46)));
        return view;
    }

    private TextView metaLabel(String text) {
        // Etiqueta para fecha, hora o usuario dentro de tarjetas.
        TextView view = label(text, 12, R.color.cyber_text, false);
        view.setBackgroundResource(R.drawable.bg_pending_meta);
        view.setPadding(dp(10), dp(7), dp(10), dp(7));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private TextView label(String text, int sp, int color, boolean bold) {
        // Helper para crear TextView desde Java sin repetir configuracion.
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(sp);
        label.setTextColor(getColor(color));
        if (bold) label.setTypeface(null, android.graphics.Typeface.BOLD);
        return label;
    }

    private TextView weightedLabel(String text, float weight) {
        // TextView con peso usado en filas del dialogo de confirmacion.
        TextView view = label(text, 14, R.color.white, false);
        view.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
        return view;
    }

    private void clearTableBody(TableLayout table) {
        // Borra todas las filas excepto la primera, que corresponde al encabezado.
        while (table.getChildCount() > 1) {
            table.removeViewAt(1);
        }
    }

    private void clearLinearBody(LinearLayout container) {
        // Borra todas las filas excepto la primera, que corresponde al encabezado.
        while (container.getChildCount() > 1) {
            container.removeViewAt(1);
        }
    }

    private String textOf(EditText editText) {
        // Obtiene texto limpio de un EditText.
        return editText.getText().toString().trim();
    }

    private void toast(String message) {
        // Mensajes cortos para retroalimentacion de acciones.
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        // Convierte dp a pixeles para crear vistas desde Java.
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private android.text.TextWatcher simpleTextWatcher(Runnable afterChanged) {
        // TextWatcher simple: ejecuta una accion despues de escribir en un buscador.
        return new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                afterChanged.run();
            }
        };
    }
}
