leftKeyCodes = [49, 81, 65, 90, 50, 87, 83, 88, 51, 69, 68, 67, 52, 82, 70, 86, 53, 84, 71, 66];


tic

names = {
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_170217.txt';  % half-speed
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_170550.txt';  % half-speed
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_171527.txt';  % half-speed
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_171925.txt';  % half-speed
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_173302.txt';  % half-speed
'../../data/activityClassification/typingSpeed/tmp/phonewear_v1_wearbk_20150512_173544.txt';  % half-speed
};

clf
hold on
for i = 1:6
    data = csvreadEX(names{i});
    fprintf('read file %d\n', i);
    plot(data(:,3), repmat(i, size(data, 1), 1), 'bo')
    fprintf('plot file %d\n', i);
end