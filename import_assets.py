import shutil
import os

material_dir = os.path.expanduser('~/material-design-icons-2.0/')
dest_dir = 'app/src/main/res/drawable-'
densities = ['xxxhdpi', 'xxhdpi', 'xhdpi', 'hdpi', 'mdpi']

def copy_material_icon(group, name):
    for d in range(0, len(densities) - 1):
        ds = densities[d]
        print (name)
        shutil.copy(material_dir + group + '/drawable-' + ds + '/' + name + '_white_36dp.png', dest_dir + ds + '/' + name + '.png')
        shutil.copy(material_dir + group + '/drawable-' + ds + '/' + name + '_black_36dp.png', dest_dir + ds + '/' + name + '_black.png')

copy_material_icon('action', 'ic_settings')
copy_material_icon('action', 'ic_search')
copy_material_icon('action', 'ic_delete')
copy_material_icon('action', 'ic_info')
copy_material_icon('action', 'ic_home')
copy_material_icon('toggle', 'ic_star')
copy_material_icon('content', 'ic_add')
copy_material_icon('content', 'ic_save')
copy_material_icon('content', 'ic_clear')
copy_material_icon('file', 'ic_file_download')
copy_material_icon('file', 'ic_file_upload')
copy_material_icon('editor', 'ic_mode_edit')
copy_material_icon('navigation', 'ic_expand_more')
copy_material_icon('image', 'ic_rotate_right')
copy_material_icon('alert', 'ic_warning')
