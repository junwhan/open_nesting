#Set max number of nodex ....
min=24
max=120
step=24
#Start batch
for r in 50
do
	for ((  n = min;  n <= max;  n+=step  ))
	do
		echo ~~~~~~~~~~~~~~ START [$n] ~~~~~~~~~~~~~~~~
		#Forwarding Parameters ....
		./script.sh -n $n -r $r $*
		echo ~~~~~~~~~~~~~~ DONE [$n] ~~~~~~~~~~~~~~~~
	done
done
