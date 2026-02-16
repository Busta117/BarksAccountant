# BarksAccountant

# Screen Tree

- login
- main app(tabbar): 
	- listado de ventas
		- detalle venta
		- nuevo/editar venta
	- lista de compras
		- detalle compra
		- nuevo/editar compra
	- settings
		- lista productos
			- editar/agregar producto
		- lista clientes
			- editar/agregar cliente

## Screen details:
### Login
**visual:** this screen only have 1 textfield that is "user id" and a login button
**logic**: el user id se guarda en local y se pone por defecto si existe, el boton login no se activa hasta que haya algo en el texto, cuando presiona el login va a validar el usuario con firestore y si existe entonces lo guarda en  local para recuperarlo en un futuro, si el usuario no existe mostrar un mensaje toast diciendo que el usuario no existe, si el usuario existe entonces cambiar el root de la app para mostrar el tabbar

### Main Tabbar
el tabbar tiene 3 vistas: ventas, compras y settings, cada uno con su vista root que voy a describir a continuacion, cada root tiene su propia navegacion en forma de push y pop, quiere decir que el tabbar siempre está visible

### Ventas tabbar root (Listado de ventas)
la vista root del tabbar es el listado de ventas donde cada venta mostrar un poco de detalle, el nombre del cliente, la fecha y el valor, a cada celda si el pedido no se a pagado, ponerle una linea vertical al lado izquierdo de toda la celda de color rojo.
esta vista tiene un titulo tambien que sea algo como Ventas, y tendrá a lado derecho un simbolo + para agregar un nuevo pedido, en forma de navigation bar.
cuando toca cada uno de los pedidos va a la vista del detalle de pedido, en forma de navegación.

### Detalle de Venta
el detalle de venta tiene un titulo que sea "Detalle de Venta" con un boton a la derecha que sea un icono de lapiz para editarlo, esto en forma de navigation bar.
el detalle de la venta tendrá esta información: nombre del cliente, responsable, listado de los nombres de productos vendidos con su cantidad y precio unitario pequeño, tambien tendrá el precio total del pedido, fecha de pedido, fecha de entrega, y si está pago o no.
al fnal 3 botones, uno para marcarlo como pago, este va a preguntar validando si lo quiere marcar como pago o no, otro para marcar como entregado(pide confirmación y al darle si entonces modifica la fecha de entrega de la venta), y el otro boton para exportar, esta opcion se hará mas adelante.
el boton del navigation bar de editar va a la misma vista de creación pero con los datos llenos

### nuevo/editar venta
aqui tendrá un titulo en forma de navigation bar que sea "nueva venta" para cuando voy a crear una venta nueva y "editar venta" cuando estoy editando. es un formulario donde tiene la sigiente información:
nombre cliente: obligatorio, este es un selector que muestra una ventana emergente con el listado de clientes(solo nombre) y al seleccionarlo se pone el nombre en el field.
responsable: opcional
fecha pedido: selector de solo dia, por defecto el dia actual
fecha entrega: opcional, es un selector de solo dia pero puede no existir
productos: es una lista embebida donde se tiene un listado de productos(nombre y precio unitario a la izquierda y dos botones para aumentar o reducir la cantidad, no puede ser menos que 1, si está en uno el boton de menos se convierte en eliminar y se elimina la celda, si es mas de 1 los botones son un + y un -) al lado derecho de la cantidad aparece el  precio final de ese producto(cantidad x precio unitario), por debajo del ultimo elemento de la lista de productos está un boton de agregar producto que mostrara una ventana emergente con el listado de productos disponibles para agregar y al seleccionarlo aparecerá en la tabla.
al final del todo un boton de guardar, que va a guardar el firestore este pedido con un id autogenerado.
una vez guardado se devuelve a la vista anterior en la navegación

### Compras tabbar root (Listado de compras)
la vista root de este tab es el listado de compras, se muestra un titulo que sea "Compras" en forma de navigation bar con un botón a la derecha que sea un + para agregar nueva compra.
cada compra aparece con un titulo, descripcion, fecha y valor
cada celda tendrá la opcion de swipe to edit, donde si le doy swipe competo de derecha a izquierda va a la pantalla de editar/agregar compra con la info llena, la navegacion en forma de push

### editar/agregar venta
esta vista tiene un titulo de "editar venta" si estoy en modo edición y "nueva venta" si voy a agregar una nueva, con la siguiente información en un formulario
titulo: obligatorio
descripcion: opcional
valor: obligatorio
fecha: obligatorio, un selector de dia, por defecto la fecha actual

### settings tabbar root
el root de este tab es un listado de opciones, donde la primera celda es el ID del usuario que inicio sesion, un poco mas grande, para mostrar que es el usuario logueado, luego las opciones son productos, clientes logout.
la opcion de productos va a ir en forma de navegación a otra pantalla de Productos.
la opcion de clientes va a ir en forma de navigación a otra pantalla de Clientes,
la opcion de logout va a pedir confirmación y si se quiere va a borrar localmente el user id y va a cambiar el root de la app por la pantalla de login

### productos
es un listado con un titulo de forma de navigation bar con texto "Productos" con una opcion a la derecha de + para agregar uno nuevo, el listado muestra el titulo del producto y su precio por unidad. si toco cualquierda de estos va a la misma pantalla de agregar producto con la info llena

### agregar/editar producto
esta pantalla tiene un titulo en forma de navigation bar, "nuevo producto" para agregar nuevo y "editar producto" para cuando estoy editando
es un formulario con nombre y precio, y un boton abajo que sea guardar, ambos fields obligatorios. al guardar se guarda en firestore con un id autogenerado, luego vemos la logica de estos datos

### clientes
es un listado con un titulo de forma de navigation vbar con texto "Clientes" con una iopcion a la derecha de + para agregar uno nuevo, ell istado nmuestra el nombre del cliente, su responsable si lo tiene, si toco a alguno va a la misma pantalla de agregar pero con la info llena en forma de push

### editar/agregar cliente
esta pantalla tiene un tituno en forma de navigation bar, "editar cliente" para cuando voy a editar y "nuevo cliente" para cuando voy a crear uno, el formulario tiene:
nombre: obligatorio
responsable: opcional
nif: oopcional
direccion: opcional
an final tierne un boton de guardar, que igual que todos los datos se guardan en firestore con un id autogenerado y se hace pop a la antalla anterior


## Firestore
la base de datos tiene como padre el id del usuario, todos los datos van a estar linkeados a este, por ahora no hay integración vamos a guardar la info localmente en estructura de datos, pero cuando integremos vamos a hacer la persistencia pero tenemos que tener en cuenta esto, que vamos a poder guardar y recuperar la info, y tenemos que extraer este layer para poderlo hacer mas facil, que independientemente si mas adelante cambiamos de firestore por un backend sea facil de migrar