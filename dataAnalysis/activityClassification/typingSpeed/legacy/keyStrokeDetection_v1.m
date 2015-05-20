leftKeyCodes = [49, 81, 65, 90, 50, 87, 83, 88, 51, 69, 68, 67, 52, 82, 70, 86, 53, 84, 71, 66];


%rootDir = '../../data/activityClassification/typingSpeed/04281230_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/04281510_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/04281645_bo_orientation/';
%rootDir = '../../data/activityClassification/typingSpeed/05091705_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/05091730_bo/';
rootDir = '../../data/activityClassification/typingSpeed/05091830_bo/';


%sensorFile = [rootDir 'sensorRaw.txt'];
%rawSensor = csvreadEX(sensorFile);

dataAcc  = csvread([rootDir 'processed_acc.txt']);
dataGyro = csvread([rootDir 'processed_gyro.txt']);
dataGrav = csvread([rootDir 'processed_grav.txt']);
%dataMag  = csvread([rootDir 'sensorRaw_mag.txt']);
dataTyping = csvread([rootDir 'processed_typing.txt']);

%TODO: delegate as a function

%% recover the texts were typing

keyTexts = cell(size(dataTyping(:,1), 1), 1);

for i = 1:size(dataTyping, 1)
    if 33 <= dataTyping(i,2) && dataTyping(i,2) <= 122  % regular chars
        keyTexts{i} = char(dataTyping(i,2));
    elseif dataTyping(i,2) == 8  % backspace
        keyTexts{i} = '<=';
    elseif dataTyping(i,2) == 16  % shift, 1=left, 2=right
        keyTexts{i} = '\^';
    elseif dataTyping(i,2) == 32  % space
        keyTexts{i} = '\_';
    elseif dataTyping(i,2) == 13  % new line
        keyTexts{i} = '\\n';
    else
        keyTexts{i} = '@@';
    end
end

isLeftKey = ismember(dataTyping(:,2), leftKeyCodes)  ...  % regular left keys
            | (dataTyping(:,2) == 16 & dataTyping(:,4) == 1);

dataTypingLeft = dataTyping(isLeftKey, :);

leftKeyTexts = keyTexts(isLeftKey);

%TODO: delegate as a function

%% algorithm start. coarse filter, threshold based

accIdxs = (dataAcc(:,5) < 8.7 | dataAcc(:,5) > 10.3);     % z-acc
gyroIdxs = (dataGyro(:,3) < -0.9 | dataGyro(:,3) > 0.9);  % x-gyro






%% stop here
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
    text(dataTyping(i,1), 13, keyTexts{i});
end

plot( [dataTyping(1,1), dataTyping(end,1)], [ 10.3  10.3], 'k--' )
plot( [dataTyping(1,1), dataTyping(end,1)], [ 8.7   8.7], 'k--' )

%% gyro
clf
hold on
%plot(dataGyro(1:end,2), dataGyro(1:end,5), 'b-o');
%plot(dataGyro(1:end,2), dataGyro(1:end,4), 'g-o');
plot(dataGyro(1:end,2), dataGyro(1:end,3), 'r-o');

plot(dataTyping(:,1), repmat(-4, length(dataTyping), 1), 'kx');
plot(dataTypingLeft(:,1), repmat(-4.5, length(dataTypingLeft), 1), 'mx');
for i = 1:size(dataTyping, 1)
    text(dataTyping(i,1), -5, keyTexts{i});
end
ylim([-20 20])

plot( [dataTyping(1,1), dataTyping(end,1)], [ 0.9  0.9], 'k--' )
plot( [dataTyping(1,1), dataTyping(end,1)], [-0.9 -0.9], 'k--' )

plot(dataAcc(accIdxs, 2), repmat(-6, sum(accIdxs), 1), 'go')
plot(dataGyro(gyroIdxs, 2), repmat(-6.5, sum(gyroIdxs), 1), 'go')
