tic
data = csvreadEX('/Users/timestring/Documents/androidWear/classifyTyping/phonewear_v0_20150422_213509.txt');
toc
%data = csvreadEX('/Users/timestring/Documents/androidWear/classifyTyping/x.txt');

return

%% separate into different sensor data
% acc     = 1
% gyro    = 4
% gravity = 9
dataAcc  = data(data(:,2) == 1, :);
dataGyro = data(data(:,2) == 4, :);
dataGrav = data(data(:,2) == 9, :);

dataAcc  = dataAcc(:,3:end);
dataGyro = dataGyro(:,3:end);
dataGrav = dataGrav(:,3:end);

t = dataAcc(1,1);
dataAcc(:,1)  = (dataAcc(:,1) - t) * 1e-9;
dataGyro(:,1) = (dataGyro(:,1) - t) * 1e-9;
dataGrav(:,1) = (dataGrav(:,1) - t) * 1e-9;

%% plot x,y,z
clf
subplot(3, 1, 1)
hold on
plot(dataAcc(1:10:end,1), dataAcc(1:10:end,2), 'r.');
plot(dataAcc(1:10:end,1), dataAcc(1:10:end,3), 'g.');
plot(dataAcc(1:10:end,1), dataAcc(1:10:end,4), 'b.');

subplot(3, 1, 2)
hold on
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,2), 'r.');
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,3), 'g.');
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,4), 'b.');
ylim([-20 20])

subplot(3, 1, 3)
hold on
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,2), 'r.');
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,3), 'g.');
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,4), 'b.');

%%
startSec = 400;
endSec = 460;

clf
subplot(3, 1, 1)
hold on
idx = (startSec <= dataAcc(:,1) & dataAcc(:,1) <= endSec);
plot(dataAcc(idx,1), dataAcc(idx,2), 'r-');
plot(dataAcc(idx,1), dataAcc(idx,3), 'g-');
plot(dataAcc(idx,1), dataAcc(idx,4), 'b-');

subplot(3, 1, 2)
hold on
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,2), 'r.');
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,3), 'g.');
plot(dataGyro(1:10:end,1), dataGyro(1:10:end,4), 'b.');
ylim([-20 20])

subplot(3, 1, 3)
hold on
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,2), 'r.');
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,3), 'g.');
plot(dataGrav(1:10:end,1), dataGrav(1:10:end,4), 'b.');