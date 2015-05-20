leftKeyCodes = [49, 81, 65, 90, 50, 87, 83, 88, 51, 69, 68, 67, 52, 82, 70, 86, 53, 84, 71, 66];

rootDir = '../../data/activityClassification/keystrokeStudy/typingData/';

%rootDir = '../../data/activityClassification/typingSpeed/20150514_172241_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/20150514_192008_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/20150514_192546_bo/';
%rootDir = '../../data/activityClassification/typingSpeed/20150514_200058_Paul/';
%rootDir = '../../data/activityClassification/typingSpeed/20150514_200220_Paul/';
%rootDir = '../../data/activityClassification/typingSpeed/20150514_200403_Paul/';  %no data
%folderName = '20150518_145348_bo_v3';
folderName = '20150518_153410_bo_v3';

folderPath = [rootDir folderName '/'];

%sensorFile = [rootDir 'sensorRaw.txt'];
%rawSensor = csvreadEX(sensorFile);

rawAcc    = csvread([folderPath 'watchAcc.txt']);
rawGyro   = csvread([folderPath 'watchGyro.txt']);
rawGrav   = csvread([folderPath 'watchGrav.txt']);
%dataMag    = csvread([folderPath 'watchMag.txt']);
rawTyping = csvread([folderPath 'typingEvent.txt']);
timeWatch   = csvread([folderPath 'timeFromWatch.txt']);
timeDesktop = csvread([folderPath 'timeFromDesktop.txt']);

offset = 2.772;
offsetFile = [folderPath 'offset.txt'];
if exist(offsetFile, 'file')
    offset = csvread(offsetFile);
end

%TODO: delegate as a function

%% align start point to time zero, use watch as reference point
timeWatchOffset = timeWatch(1) / 1000;
timeDesktopOffset = timeWatchOffset + mean(timeDesktop - timeWatch) / 1000 + offset;

dataAcc = rawAcc;
dataAcc(:,1) = dataAcc(:,1) / 1e3 - timeWatchOffset;
dataAcc(:,2) = dataAcc(:,2) / 1e9 - timeWatchOffset;
dataGyro = rawGyro;
dataGyro(:,1) = dataGyro(:,1) / 1e3 - timeWatchOffset;
dataGyro(:,2) = dataGyro(:,2) / 1e9 - timeWatchOffset;
dataGrav = rawGrav;
dataGrav(:,1) = dataGrav(:,1) / 1e3 - timeWatchOffset;
dataGrav(:,2) = dataGrav(:,2) / 1e9 - timeWatchOffset;
%dataMag = rawMag;
%dataMag(:,1) = dataMag(:,1) / 1e3 - timeWatchOffset;
%dataMag(:,2) = dataMag(:,2) / 1e9 - timeWatchOffset;
dataTyping = rawTyping;
dataTyping(:,1) = dataTyping(:,1) / 1000 - timeDesktopOffset;

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

return;

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
plot( [dataTyping(1,1), dataTyping(end,1)], [   0    0], 'k--' )
plot( [dataTyping(1,1), dataTyping(end,1)], [-0.9 -0.9], 'k--' )

plot(dataAcc(accIdxs, 2), repmat(-6, sum(accIdxs), 1), 'go')
plot(dataGyro(gyroIdxs, 2), repmat(-6.5, sum(gyroIdxs), 1), 'go')
