import { Component, inject, Input, OnInit } from '@angular/core';
import { AssetService } from '../../services/asset.service';

@Component({
  selector: 'piao-icon',
  imports: [],
  templateUrl: './icon.component.html',
  styleUrl: './icon.component.scss',
})
export class IconComponent implements OnInit {
  @Input() icon!: string;
  @Input() iconContainerClass: string = 'icon-container';
  @Input() iconImgClass: string = 'icon-img';
  @Input() iconColor: string = 'black';
  iconPath: string = '';
  fileExtension: string = '.svg';

  private readonly asset = inject(AssetService);

  ngOnInit(): void {
    if (this.icon) {
      this.iconPath = this.asset.url(`icon/${this.icon}${this.fileExtension}`);
    }
  }
}
