#!/bin/bash 

echo Instalando NGINX

sudo apt-get install -y --force-yes nginx

echo Configurando NGINX com proxy reverso SNI

sudo cp sites-available/* /etc/nginx/conf.d/
sudo cp ssl/* /etc/nginx/ssl/

echo Iniciando NGINX
sudo service nginx restart