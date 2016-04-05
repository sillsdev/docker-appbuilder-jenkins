# -*- mode: ruby -*-
# vi: set ft=ruby :
# Copyright (c) 2015 SIL International
#
#   Permission is hereby granted, free of charge, to any person obtaining a copy
#   of this software and associated documentation files (the "Software"), to deal
#   in the Software without restriction, including without limitation the rights
#   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#   copies of the Software, and to permit persons to whom the Software is
#   furnished to do so, subject to the following conditions:
#
#   The above copyright notice and this permission notice shall be included in
#   all copies or substantial portions of the Software.
#
#   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
#   THE SOFTWARE.


# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  # config.vm.box = "ubuntu/trusty64"
  config.vm.box = "AlbanMontaigu/boot2docker"
  config.vm.box_version = "= 1.8.2"

  # The AlbanMontaigu/boot2docker box has not been set up as a Vagrant
  # 'base box', so it is necessary to specify how to SSH in.
  config.ssh.username = "docker"
  config.ssh.password = "tcuser"
  config.ssh.insert_key = true


  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080


  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"
  #config.vm.network "private_network", ip: "192.168.70.249", nic_type: "virtio"

  # These lines override a virtual NIC that the AlbanMontaigu/boot2docker box
  # creates by default. If you need to change the the box's IP address (which
  # is necessary to run separate, simulataneous instances of this Vagrantfile),
  # do it here.
  config.vm.provider "virtualbox" do |v, override|
    # Create a private network for accessing VM without NAT
    override.vm.network "private_network", ip: "192.168.70.241", id: "default-network", nic_type: "virtio"
    end

  # boot2docker default is 1.5G.  build agent needs that much.
  # Limit CPU usage to up to 50% of host CPU
  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--cpuexecutioncap", "50"]
end

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"


  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.

  # Synced folders for container data.

  #config.vm.synced_folder "./data", "/data",
   # 1000 is the jenkins user/group in the jenkins container
   #mount_options: ["uid=1000","gid=1000","fmode=755","dmode=755"]


  config.vm.provider "virtualbox" do |vb|
  # A fix for speed issues with DNS resolution:
  #   http://serverfault.com/questions/453185/vagrant-virtualbox-dns-10-0-2-3-not-working?rq=1
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]

    # Set the timesync threshold to 59 seconds, instead of the default 20 minutes.
    # 59 seconds chosen to ensure SimpleSAML never gets too far out of date.
    vb.customize ["guestproperty", "set", :id, "/VirtualBox/GuestAdd/VBoxService/--timesync-set-threshold", 59000]
  end


  # This provisioner runs on the first `vagrant up`.
  config.vm.provision "install", type: "shell", inline: <<-SHELL
     # Copy the home directory to persistent storage
     mkdir /mnt/sda2/home
     cp -r /home/docker /mnt/sda2/home
     chown -R docker.staff /mnt/sda2/home/docker

     #Switcheroo
     mount --bind /mnt/sda2/home/docker /home/docker
     cd /home/docker

     # Download Python and Pip
     mkdir /mnt/sda2/tce-persist
     chown docker.staff /mnt/sda2/tce-persist
     chmod 775 /mnt/sda2/tce-persist

     mount --bind /mnt/sda2/tce-persist /mnt/sda2/tmp/tce/optional
     sudo -u tc tce-load -w python
     umount /mnt/sda2/tmp/tce/optional

     # Configure boot2docker's running of docker
     # (Mixing of tabs and spaces is intentional, used for the <<- operator)
     cat <<-EOF >> /var/lib/boot2docker/profile
	EXTRA_ARGS="-icc=false"
	DOCKER_TLS="no"

	EOF

     /etc/init.d/docker stop
     iptables -F
     /etc/init.d/docker start

     # Convenience
     if mount | grep /vagrant 1>/dev/null; then
       ln -s /vagrant /home/docker/vagrant
     fi
   SHELL


  # This provisioner runs on every `vagrant reload' (as well as the first
  # `vagrant up`), reinstalling from local directories
  config.vm.provision "recompose", type: "shell",
   run: "always", inline: <<-SHELL
     #Switcheroo
     mount --bind /mnt/sda2/home/docker /home/docker
     cd /home/docker

     # Install python and Docker-Compose
     mount --bind /mnt/sda2/tce-persist /mnt/sda2/tmp/tce/optional
     sudo -u tc tce-load -ic python
     umount /mnt/sda2/tmp/tce/optional

     # Use curl to install docker-compose (rather than pip, due to
     # "https://github.com/boot2docker/boot2docker/issues/1055").
     curl -sS -L https://github.com/docker/compose/releases/download/1.4.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
     chmod +x /usr/local/bin/docker-compose

     # Run docker-compose (which will pulls images)
     cd /vagrant

     # Start services
     docker-compose up -d

     echo " "
     echo "use 'vagrant ssh' to connect, and run commands"
     echo "from inside the box, e.g.:"
     echo " "
     echo "  vagrant ssh"
     echo "  cd /vagrant"
     echo "  docker-compose ps"
     echo "  docker images"

     # Finally, and importantly, stop all running dhcp clients; this box is
     # statically configured by Vagrant, and asking for dhcp will just
     # override the network settings in this Vagrantfile.
     killall udhcpc
  SHELL
end
