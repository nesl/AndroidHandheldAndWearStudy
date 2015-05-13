% This file read sensorRaw.txt directly, which has not been pre-processed
% and lead poor reading performance.

leftKeyCodes = [49, 81, 65, 90, 50, 87, 83, 88, 51, 69, 68, 67, 52, 82, 70, 86, 53, 84, 71, 66];


tic
%rootDir = '../../data/activityClassification/typingSpeed/04281230_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/04281510_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/04281645_bo_orientation/';
%rootDir = '../../data/activityClassification/typingSpeed/05091705_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/05091730_bo/';
rootDir = '../../data/activityClassification/typingSpeed/05091830_bo/';


sensorFile = [rootDir 'sensorRaw.txt'];
rawSensor = csvreadEX(sensorFile);
typingFile = [rootDir 'typingEvent.txt'];
rawTyping = csvread(typingFile);
toc

%% separate into different sensor data
% acc     = 1
% gyro    = 4
% gravity = 9
dataAcc  = rawSensor(rawSensor(:,2) == 1, :);
dataGyro = rawSensor(rawSensor(:,2) == 4, :);
dataGrav = rawSensor(rawSensor(:,2) == 9, :);

dataAcc  = dataAcc(:,3:end);
dataGyro = dataGyro(:,3:end);
dataGrav = dataGrav(:,3:end);

tsys = dataAcc(1,1);  % system time
dataAcc(:,1)  = (dataAcc(:,1) - tsys) * 1e-3;
dataGyro(:,1) = (dataGyro(:,1) - tsys) * 1e-3;
dataGrav(:,1) = (dataGrav(:,1) - tsys) * 1e-3;

tsen = dataAcc(1,2);  % sensor time
dataAcc(:,2)  = (dataAcc(:,2) - tsen) * 1e-9;
dataGyro(:,2) = (dataGyro(:,2) - tsen) * 1e-9;
dataGrav(:,2) = (dataGrav(:,2) - tsen) * 1e-9;

%%
dataTyping = rawTyping;
dataTyping(:,1) = (rawTyping(:,1) - tsys) / 1000 - 97.426;

isLeftKey = false(length(dataTyping), 1);
for key = leftKeyCodes
    isLeftKey = isLeftKey | (dataTyping(:,2) == key);
end

dataTypingLeft = dataTyping(isLeftKey, :);

return

%% plot x,y,z (sub sample)
clf
subplot(3, 1, 1)
hold on
plot(dataAcc(1:10:end,2), dataAcc(1:10:end,3), 'r.');
plot(dataAcc(1:10:end,2), dataAcc(1:10:end,4), 'g.');
plot(dataAcc(1:10:end,2), dataAcc(1:10:end,5), 'b.');

subplot(3, 1, 2)
hold on
plot(dataGyro(1:10:end,2), dataGyro(1:10:end,3), 'r.');
plot(dataGyro(1:10:end,2), dataGyro(1:10:end,4), 'g.');
plot(dataGyro(1:10:end,2), dataGyro(1:10:end,5), 'b.');
ylim([-20 20])

subplot(3, 1, 3)
hold on
plot(dataGrav(1:10:end,2), dataGrav(1:10:end,3), 'r.');
plot(dataGrav(1:10:end,2), dataGrav(1:10:end,4), 'g.');
plot(dataGrav(1:10:end,2), dataGrav(1:10:end,5), 'b.');

%% plot x,y,z 
clf
sh1 = subplot(3, 1, 1)
hold on
plot(dataAcc(1:end,2), dataAcc(1:end,3), 'r.');
plot(dataAcc(1:end,2), dataAcc(1:end,4), 'g.');
plot(dataAcc(1:end,2), dataAcc(1:end,5), 'b.');
plot(dataTyping(:,1), repmat(-10, length(dataTyping), 1), 'kx');
plot(dataTypingLeft(:,1), repmat(-9, length(dataTypingLeft), 1), 'mx');

sh2 = subplot(3, 1, 2)
hold on
plot(dataGyro(1:end,2), dataGyro(1:end,3), 'r-o');
plot(dataGyro(1:end,2), dataGyro(1:end,4), 'g-o');
plot(dataGyro(1:end,2), dataGyro(1:end,5), 'b-o');
plot(dataTyping(:,1), repmat(-10, length(dataTyping), 1), 'kx');
plot(dataTypingLeft(:,1), repmat(-9, length(dataTypingLeft), 1), 'mx');
ylim([-20 20])

sh3 = subplot(3, 1, 3)
hold on
plot(dataGrav(1:end,2), dataGrav(1:end,3), 'r-');
plot(dataGrav(1:end,2), dataGrav(1:end,4), 'g-');
plot(dataGrav(1:end,2), dataGrav(1:end,5), 'b-');
plot(dataTyping(:,1), repmat(-10, length(dataTyping), 1), 'kx');

linkaxes([sh1, sh2, sh3],'x')

%% acc
clf
hold on
plot(dataAcc(1:end,2), dataAcc(1:end,3), 'r-o');
plot(dataAcc(1:end,2), dataAcc(1:end,4), 'g-o');
plot(dataAcc(1:end,2), dataAcc(1:end,5), 'b-o');
plot(dataTyping(:,1), repmat(15, length(dataTyping), 1), 'kx');
plot(dataTypingLeft(:,1), repmat(14, length(dataTypingLeft), 1), 'mx');
for i = 1:size(dataTyping, 1)
    if 33 <= dataTyping(i,2) && dataTyping(i,2) <= 122
        text(dataTyping(i,1), 13, char(dataTyping(i,2)));
    end
end


%% gyro
clf
hold on
plot(dataGyro(1:end,2), dataGyro(1:end,5), 'b-o');
plot(dataGyro(1:end,2), dataGyro(1:end,4), 'g-o');
plot(dataGyro(1:end,2), dataGyro(1:end,3), 'r-o');

plot(dataTyping(:,1), repmat(-4, length(dataTyping), 1), 'kx');
plot(dataTypingLeft(:,1), repmat(-4.5, length(dataTypingLeft), 1), 'mx');
for i = 1:size(dataTyping, 1)
    if 33 <= dataTyping(i,2) && dataTyping(i,2) <= 122
        text(dataTyping(i,1), -5, char(dataTyping(i,2)));
    end
end
ylim([-20 20])


%% see system time and sensor time
clf
plot(dataAcc(:,1), dataAcc(:,2), '.');
xlabel('system time');
ylabel('sensor time');

%% show key statistics
stat = tabulate(dataTyping(:,2));
stat = stat(leftKeyCodes, 1:2);
bar(stat(:,2));
xlabels = cellstr(char(stat(:,1)))
set(gca, 'XTickLabel', xlabels)
set(gca, 'XTick', 1:size(stat, 1));