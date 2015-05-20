function [ result ] = csvreadEX(filename)
% allow varied number of columns

fid = fopen(filename);

tmp = {};
tline = fgets(fid);
ncol = 0;
while ischar(tline)
    tline = fgets(fid);
    %fprintf('%s %d\n', tline, length(tline));
    if length(tline) <= 1
        continue
    end
    words = strsplit(tline, ',');
    arr = cellfun(@str2num, words);
    tn = numel(arr);
    ncol = max(ncol, tn);
    tmp{end+1} = arr;
end
fclose(fid);

result = nan(numel(tmp), ncol);
for i = 1:numel(tmp)
    tn = numel( tmp{i} );
    result(i, 1:tn) = tmp{i};
end


end

