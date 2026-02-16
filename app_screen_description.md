# BarksAccountant

# Screen Tree

- login
- main app(tabbar):
	- listado de ventas
		- detalle venta
		- nuevo/editar venta (con opción de eliminar en edición)
	- lista de compras
		- nuevo/editar compra (con opción de eliminar en edición)
	- settings
		- lista productos
			- editar/agregar producto (con opción de eliminar en edición)
		- lista clientes
			- editar/agregar cliente (con opción de eliminar en edición)

## Screen details:
### Login
**visual:** esta pantalla tiene 2 textfields: "App ID" y "Nombre de persona", y un botón de login.
**logic**: el App ID y el nombre se guardan en local y se ponen por defecto si existen. El botón login no se activa hasta que ambos campos tengan contenido. Cuando presiona login, valida que el App ID exista en la colección `app_ids` de Firestore. Si existe, guarda ambos valores en local para recuperarlos en el futuro. Si el App ID no existe, muestra un mensaje toast/snackbar diciendo que no existe. Si existe, cambia el root de la app para mostrar el tabbar. El App ID determina el scope de datos (todos los datos se guardan bajo `apps/{appId}/`).

### Main Tabbar
El tabbar tiene 3 vistas: ventas, compras y settings, cada uno con su vista root que voy a describir a continuación, cada root tiene su propia navegación en forma de push y pop, quiere decir que el tabbar siempre está visible.

### Ventas tabbar root (Listado de ventas)
La vista root del tabbar es el listado de ventas donde cada venta muestra un poco de detalle: el nombre del cliente, la fecha y el valor. A cada celda, si el pedido no se ha pagado, se le pone una línea vertical al lado izquierdo de toda la celda de color rojo.
Esta vista tiene un título "Ventas" en forma de navigation bar, con un símbolo + a la derecha para agregar un nuevo pedido.
Cuando toca cada uno de los pedidos va a la vista del detalle de pedido, en forma de navegación.
**Empty state:** cuando no hay ventas, mostrar un texto centrado "No hay ventas".

### Detalle de Venta
El detalle de venta tiene un título "Detalle de Venta" con un botón a la derecha que sea un icono de lápiz para editarlo, en forma de navigation bar.
El detalle tendrá: nombre del cliente, responsable, listado de los nombres de productos vendidos con su cantidad y precio unitario, precio total del pedido, fecha de pedido, fecha de entrega, y si está pago o no.
Al final 3 botones: uno para marcarlo como pago (con confirmación), otro para marcar como entregado (con confirmación, modifica la fecha de entrega), y otro para exportar (pendiente).
El botón de editar va a la misma vista de creación pero con los datos llenos.

### nuevo/editar venta
Título en navigation bar: "Nueva Venta" o "Editar Venta" según corresponda. Formulario con:
- **nombre cliente**: obligatorio, selector que muestra ventana emergente con listado de clientes (solo nombre).
- **responsable**: opcional.
- **fecha pedido**: selector de solo día, por defecto el día actual.
- **fecha entrega**: opcional, selector de solo día.
- **productos**: lista embebida con botones +/- para cantidad. Si cantidad es 1, el botón - se convierte en eliminar (icono de trash). A la derecha de la cantidad aparece el precio final (cantidad × precio unitario). Debajo hay un botón "Agregar Producto" que muestra ventana emergente con productos disponibles.
- **total**: suma de todos los productos.
- **guardar**: guarda en Firestore y vuelve atrás.
- **eliminar** (solo en modo edición): botón rojo debajo de guardar, pide confirmación con alert/dialog, al confirmar elimina el registro de Firestore y vuelve atrás.

### Compras tabbar root (Listado de compras)
La vista root de este tab es el listado de compras. Título "Compras" en navigation bar con botón + a la derecha.
Cada compra aparece con título, descripción, fecha y valor.
Cada celda tiene la opción de swipe-to-edit (iOS) o simplemente tap (ambas plataformas) para ir a editar/agregar compra con la info llena.
**Empty state:** cuando no hay compras, mostrar un texto centrado "No hay compras".

### editar/agregar compra
Título: "Editar Compra" o "Nueva Compra" según corresponda. Formulario con:
- **titulo**: obligatorio.
- **descripción**: opcional.
- **valor**: obligatorio (numérico, mayor a 0).
- **fecha**: obligatorio, selector de día, por defecto la fecha actual.
- **guardar**: guarda en Firestore y vuelve atrás.
- **eliminar** (solo en modo edición): botón rojo debajo de guardar, pide confirmación con alert/dialog, al confirmar elimina el registro y vuelve atrás.

### settings tabbar root
El root de este tab es un listado de opciones. La primera sección muestra el App ID (grande) y el nombre del usuario logueado. Las opciones son: Productos, Clientes, y Cerrar sesión.
- **Productos**: navega en push a la pantalla de Productos.
- **Clientes**: navega en push a la pantalla de Clientes.
- **Cerrar sesión**: pide confirmación y si acepta borra localmente el App ID y nombre, cambiando el root a login.

### productos
Listado con título "Productos" en navigation bar con botón + a la derecha. Muestra nombre del producto y su precio por unidad. Si toco cualquiera va a la pantalla de agregar/editar producto con la info llena.
**Empty state:** cuando no hay productos, mostrar un texto centrado "No hay productos".

### agregar/editar producto
Título: "Nuevo Producto" o "Editar Producto" según corresponda. Formulario con:
- **nombre**: obligatorio.
- **precio**: obligatorio (numérico, mayor a 0).
- **guardar**: guarda en Firestore y vuelve atrás.
- **eliminar** (solo en modo edición): botón rojo debajo de guardar, pide confirmación, al confirmar elimina de Firestore y vuelve atrás.

### clientes
Listado con título "Clientes" en navigation bar con botón + a la derecha. Muestra el nombre del cliente y su responsable si lo tiene. Si toco alguno va a la pantalla de agregar/editar con la info llena.
**Empty state:** cuando no hay clientes, mostrar un texto centrado "No hay clientes".

### editar/agregar cliente
Título: "Editar Cliente" o "Nuevo Cliente" según corresponda. Formulario con:
- **nombre**: obligatorio.
- **responsable**: opcional.
- **nif**: opcional.
- **dirección**: opcional.
- **guardar**: guarda en Firestore y vuelve atrás.
- **eliminar** (solo en modo edición): botón rojo debajo de guardar, pide confirmación, al confirmar elimina de Firestore y vuelve atrás.


## Firestore

### Estructura de datos
```
Firestore root/
├── app_ids/
│   └── {appId}/         # Documento vacío, solo para validar existencia del App ID
└── apps/
    └── {appId}/         # Datos del app scoped por App ID
        ├── sales/       # Colección de ventas
        │   └── {saleId}
        ├── purchases/   # Colección de compras
        │   └── {purchaseId}
        ├── products/    # Colección de productos
        │   └── {productId}
        └── clients/     # Colección de clientes
            └── {clientId}
```

### Reglas de seguridad
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /app_ids/{appId} {
      allow read: if true;
    }
    match /apps/{appId}/{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Patrón de acceso
Todos los datos están aislados por App ID. El layer de repositorio abstrae Firestore para facilitar migración futura a otro backend. Las interfaces de repositorio están en `shared/.../data/repository/` y las implementaciones de Firestore en el mismo paquete con prefijo `Firestore`.
