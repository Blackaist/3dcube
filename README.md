# 3D Cube on Android #

## Some basics ##
Here three methods of shading
### 1. Flat (Simple, Lambert) shading: ###

for every vertex on polygon of Cube we use some math.

![Flat](https://user-images.githubusercontent.com/17519552/36176861-0dc04f0e-1125-11e8-9c52-6dd51dd1a59f.jpg)
### 2. Guro shading ###

Like Flat shading, but here we are using color interpolation between 4 vertex points.
![Guro](https://user-images.githubusercontent.com/17519552/36176865-0f832712-1125-11e8-9376-ea8587232b11.jpg)
### 3. Phong shading ###

Like Flat shading, but here we are using normal interpolation between 4 vertex points.
![Phong 1](https://user-images.githubusercontent.com/17519552/36176871-1096674a-1125-11e8-9e0a-f855b298098f.jpg)
and
![Phong 2](https://user-images.githubusercontent.com/17519552/36176872-115c11ac-1125-11e8-9448-3687260fcb7d.jpg)


## warning! ## 
Distance - distance between (0, 0, 0) (but we move the axis to (0, 0, 200)) and projector (light point). But i use distance between 
vertex and projector.

Light Vector - vector between (0, 0, 0) (also (0, 0, 200)) and projector. But i use distance between vertex and projector.
